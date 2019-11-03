#include<iostream>
#include<vector>
#include<string>
#include<unistd.h>
#include<string.h>
#include<sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <thread>
#include "Server.h"

Server::Server(uint16_t portNumber) {
    memset(&address, 0, sizeof(struct sockaddr));
    memset(&isIdBusy, 0, MAX_NUMBER_OF_CONCURRENT_CLIENTS * sizeof(bool));
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = htonl(INADDR_ANY);
    address.sin_port = htons(portNumber);
    reuseAddressValue = 1;
    for (int i = 0; i < MAX_NUMBER_OF_CONCURRENT_CLIENTS; i++) {
        connectionIdsToUserIndexesMap[i] = -1;
    } 
}

void Server::initialize(int connectionQueueSize) {
    int bindResult;
    int listenResult;
    int setSocketOptionsResult;
    socketDescriptor = socket(AF_INET, SOCK_STREAM, 0);
    if (socketDescriptor < 0) {
        //throw socketInitalizationError;
        fprintf(stderr, "Błąd przy próbie utworzenia gniazda..\n");
        exit(1);
    }
    setsockopt(socketDescriptor, SOL_SOCKET, SO_REUSEADDR, (char *)&reuseAddressValue, sizeof(reuseAddressValue));
    if (setSocketOptionsResult < 0) {
        //throw socketOptionsSettingError;
        fprintf(stderr, "Błąd przy próbie ustawienia opcji gniazda..\n");
        exit(1);
    }
    bindResult = bind(socketDescriptor, (struct sockaddr*) &address, sizeof(struct sockaddr));
    if (bindResult < 0) {
        //throw socketBindingError;
        fprintf(stderr, "Błąd przy próbie przypisania adresu do gniazda..\n");
        exit(1);
    }
    listenResult = listen(socketDescriptor, connectionQueueSize);
    if (listenResult < 0) {
        //throw listeningError;
        fprintf(stderr, "Błąd przy próbie ustawienia nasluchujacego gniazda..\n");
        exit(1);
    }
}

int Server::acceptConnection() {
    int assignedId;
    int incomingConnectionDescriptor;
    incomingConnectionDescriptor = accept(socketDescriptor, NULL, NULL);
    if (incomingConnectionDescriptor < 0) {
        //throw acceptError;
        fprintf(stderr, "Błąd przy akceptacji polaczenia..\n");
        exit(1);
    }
    std::cout << "Zaakceptowany\n";
    assignedId = assignConnectionId();
    connectionSocketDescriptors[assignedId] = incomingConnectionDescriptor;
    std::cout << assignedId << " <- Przydzielone Id\n";
    return assignedId;
}

int Server::assignConnectionId() {
    for (int i = 0; i < MAX_NUMBER_OF_CONCURRENT_CLIENTS; i++) {
        if (!isIdBusy[i]) {
            isIdBusy[i] = true;
            return i;
        }
    }
    std::cout << "FAIL\n";
    return -1;
}

/*void Server::handleConnection(int connectionId) {
    std::thread clientThread(threadRoutine, connectionId);
    //int threadCreateResult;
    //thread_data_t *threadArgs = new thread_data_t;
    //threadArgs->connectionId = connectionId;
    threadCreateResult = pthread_create(&thread, NULL, threadRoutine, (void *)threadArgs);
    if (threadCreateResult < 0) {
        fprintf(stderr, "Błąd przy próbie tworzenia watku..\n");
        exit(1);
    }
}*/

void Server::threadRoutine(int connectionId) {
    //pthread_detach(pthread_self());
    std::string login, password, listOfOnlineUsers;
    char receivedMessageBuffer[BUFFER_SIZE];
    char sentMessageBuffer[BUFFER_SIZE];
    memset(receivedMessageBuffer, 0, BUFFER_SIZE);
    int readResult;
    unsigned int userIndex;
    bool areCredentialsCorrect = false, isUserLoggedInAlready = false;
    //struct thread_data_t *threadData = (struct thread_data_t*) threadArgs;
    int clientSocketDescriptor = connectionSocketDescriptors[connectionId];
    std::cout << "w watku przed petla\n";
    while (receivedMessageBuffer[0] != 'q') {
        memset(receivedMessageBuffer, 0, BUFFER_SIZE);
        memset(sentMessageBuffer, 0, BUFFER_SIZE);
        std::cout << "beforeread\n";
        readResult = read(clientSocketDescriptor, receivedMessageBuffer, BUFFER_SIZE);
        std::cout << readResult << " <- Ilosc odczytanych bajtow\n";
        std::cout << "Odczytana wiadomosc -> " << receivedMessageBuffer;
        if (readResult < 0) {
            //throw readingError;
            fprintf(stderr, "Błąd przy próbie odczytu wiadomosci..\n");
            exit(1);
        }
        if (receivedMessageBuffer[0] == 'q') {
            std::cout << "no to klient chyba poszedl...\n";
            
        }
        switch (receivedMessageBuffer[0]) {
        case 'l':
            parseLoginAndPassword(readResult - 3, receivedMessageBuffer, &login, &password);
            areCredentialsCorrect = checkIfCredentialsAreCorrectAndAddUserDataIfHeIsNew(login, password);
            userIndex = getUserIndex(login);
            isUserLoggedInAlready = checkIfUserIsLoggedInAlready(userIndex);
            userInformation[userIndex].socketDescriptor = clientSocketDescriptor;
            connectionIdsToUserIndexesMap[connectionId] = userIndex;
            sendResponseToClient(clientSocketDescriptor, areCredentialsCorrect);
            if (areCredentialsCorrect && !isUserLoggedInAlready) {
                listOfOnlineUsers = getListOfOnlineUsers(userIndex);
                announceStateChange(userIndex, clientSocketDescriptor, "i ");
                sendListOfOnlineUsersToClient(clientSocketDescriptor, listOfOnlineUsers);
                setUserAsOnline(userIndex);
            }
            if (isUserLoggedInAlready) {
                sendUserAlreadyLoggedInMessage(clientSocketDescriptor);
            }
            break;
        case 'o':
            announceStateChange(userIndex, clientSocketDescriptor, "o ");
            setUserAsOffline(userIndex);
            connectionIdsToUserIndexesMap[userIndex] = -1;
            break;
        case 'm':
            sendMessage(receivedMessageBuffer, userIndex);
            break;
        }
    }

    if (userInformation[userIndex].isOnline) {
        announceStateChange(userIndex, clientSocketDescriptor, "o ");
        setUserAsOffline(userIndex);
        connectionIdsToUserIndexesMap[userIndex] = -1;
    }
    isIdBusy[connectionId] = false;
    userInformation[userIndex].socketDescriptor = -1;
    //delete threadData;
    std::cout << "klient rozlacza sie\n";
    //pthread_exit(NULL);
}

void Server::parseLoginAndPassword(int numberOfReadCharacters, char *message, std::string *login, std::string *password) {
    *login = "";
    *password = "";
    unsigned int iterator;
    for (iterator = 1; iterator < numberOfReadCharacters; iterator++) {
        if (message[iterator] != ' ') {
            *login += message[iterator];
        }
        else {
            iterator++;
            break;
        }
    }
    for (; iterator < numberOfReadCharacters; iterator++) {
        *password += message[iterator];
    }
    std::cout << *login << " <- odczytany login\n";
    std::cout << *password << " <- odczytane haslo\n";
}

bool Server::checkIfCredentialsAreCorrectAndAddUserDataIfHeIsNew(std::string login, std::string password) {
    for (unsigned int i = 0; i < userInformation.size(); i++) {
        if (userInformation[i].username.compare(login) == 0) {
            if (userInformation[i].password.compare(password) == 0) return true;
            return false;
        }
    }
    userInformation.push_back(User(login, password));
    return true;
}

unsigned int Server::getUserIndex(std::string login) {
    for (unsigned int i = 0; i < userInformation.size(); i++) {
        if (userInformation[i].username.compare(login) == 0) {
            return i;
        }
    }
}

bool Server::checkIfUserIsLoggedInAlready(unsigned int userIndex) {
    return userInformation[userIndex].isOnline;
}

void Server::sendResponseToClient(int clientSocketDescriptor, bool isLoginSuccessful) {
    int writeResult = 0;
    if (isLoginSuccessful) {
        writeResult = write(clientSocketDescriptor, SUCCESSFUL_LOGIN_MESSAGE, 8);
    }
    else {
        writeResult = write(clientSocketDescriptor, BAD_PASSWORD_MESSAGE, 45);
    }
    std::cout << writeResult << " <- Dlugosc odeslanej wiadomosci\n";
    if (writeResult < 0) {
        //throw writingError;
        fprintf(stderr, "Błąd przy próbie zapisu wiadomosci..\n");
        exit(1);
    }
}

void Server::setUserAsOffline(unsigned int userIndex) {
    userInformation[userIndex].isOnline = false;
}

void Server::setUserAsOnline(unsigned int userIndex) {
    userInformation[userIndex].isOnline = true;
}

std::string Server::getListOfOnlineUsers(unsigned int userIndex) {
    std::string list = "l ";
    for (unsigned int i = 0; i < userInformation.size(); i++) {
        if (i != userIndex && userInformation[i].isOnline) {
            list = list + userInformation[i].username + " ";
        }
    }
    list += "  ";
    std::cout << list << "<- lista ludzi online\n";
    return list;
}

void Server::announceStateChange(unsigned int myIndex, int mySocketDescriptor, std::string changeType) {
    int writeResult;
    std::string message = changeType + userInformation[myIndex].username + "  ";
    char messageBuffer[BUFFER_SIZE];
    strcpy(messageBuffer, message.c_str());
    for (int i = 0; i < MAX_NUMBER_OF_CONCURRENT_CLIENTS; i++) {
        //std::cout << connectionIdsToUserIndexesMap[i] << "<- mapa id polaczen na indeksy uzytkownikow\n";
        if (isIdBusy[i] && connectionIdsToUserIndexesMap[i] != -1 && connectionSocketDescriptors[i] != mySocketDescriptor && userInformation[connectionIdsToUserIndexesMap[i]].isOnline) {
            writeResult = write(connectionSocketDescriptors[i], messageBuffer, message.length());
            if (writeResult < 0) {
                //throw writingError;
                fprintf(stderr, "Błąd przy próbie zapisu wiadomosci..\n");
                exit(1);
            }
        }
    }
}

void Server::sendListOfOnlineUsersToClient(int clientSocketDescriptor, std::string list) {
    int writeResult = 0;
    char messageBuffer[list.length() + 1];
    strcpy(messageBuffer, list.c_str());
    std::cout << messageBuffer << "<- lista ludzi online\n";
    std::cout << strlen(messageBuffer) << "<- dlugosc listy\n";
    writeResult = write(clientSocketDescriptor, messageBuffer, strlen(messageBuffer));
    std::cout << messageBuffer << " <- Dlugosc odeslanej listy\n";
    if (writeResult < 0) {
        //throw writingError;
        fprintf(stderr, "Błąd przy próbie zapisu wiadomosci..\n");
        exit(1);
    }
}

void Server::sendUserAlreadyLoggedInMessage(int clientSocketDescriptor) {
    int writeResult;
    char messageBuffer[30];
    memset(messageBuffer, 0, 30);
    strcpy(messageBuffer, "a useralreadyloggedinrip");
    writeResult = write(clientSocketDescriptor, messageBuffer, strlen(messageBuffer));

}

void Server::sendMessage(char* message, unsigned int userIndex) {
    char properMessageBuffer[BUFFER_SIZE];
    std::string recipientNick = "";
    std::string properMessage = "m " + userInformation[userIndex].username + " ";
    unsigned int iterator = 2;
    unsigned int properMessageIndex = 0;
    for (; iterator < strlen(message); iterator++) {
        if (message[iterator] == ' ') {
            iterator++;
            break;
        }
        else recipientNick += message[iterator];
    }
    for (; iterator < strlen(message); iterator++) {
        properMessage += message[iterator];
    }
    unsigned int recipientIndex;
    for (unsigned int i = 0; i < userInformation.size(); i++) {
        if (recipientNick.compare(userInformation[i].username) == 0) {
            recipientIndex = i;
            break;
        }
    }
    strcpy(properMessageBuffer, properMessage.c_str());
    int writeResult;
    writeResult = write(userInformation[recipientIndex].socketDescriptor, properMessageBuffer, properMessage.length());
    std::cout << properMessageBuffer << " <- przeslana wiadomosc\n";
    std::cout << recipientNick << "<- nick odbiorcy\n";
    if (writeResult < 0) {
        //throw writingError;
        fprintf(stderr, "Błąd przy próbie zapisu wiadomosci..\n");
        exit(1);
    }
}

Server::~Server() {

}