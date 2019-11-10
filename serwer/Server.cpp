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
        fprintf(stderr, "Błąd przy próbie utworzenia gniazda..\n");
        exit(1);
    }
    setsockopt(socketDescriptor, SOL_SOCKET, SO_REUSEADDR, (char *)&reuseAddressValue, sizeof(reuseAddressValue));
    bindResult = bind(socketDescriptor, (struct sockaddr*) &address, sizeof(struct sockaddr));
    if (bindResult < 0) {
        fprintf(stderr, "Błąd przy próbie przypisania adresu do gniazda..\n");
        exit(1);
    }
    listenResult = listen(socketDescriptor, connectionQueueSize);
    if (listenResult < 0) {
        fprintf(stderr, "Błąd przy próbie ustawienia nasluchujacego gniazda..\n");
        exit(1);
    }
}

int Server::acceptConnection() {
    int assignedId;
    int incomingConnectionDescriptor;
    incomingConnectionDescriptor = accept(socketDescriptor, NULL, NULL);
    if (incomingConnectionDescriptor < 0) {
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

void Server::threadRoutine(int connectionId) {
    std::string login, password, listOfOnlineUsers;
    std::string receivedMessage;
    std::string lastOfGluedMessages;
    std::vector<std::string> words;
    std::vector<std::string> incomingMessages;
    std::vector<std::string> gluedMessages;
    char receivedMessageBuffer[BUFFER_SIZE];
    char sentMessageBuffer[BUFFER_SIZE];
    memset(receivedMessageBuffer, 0, BUFFER_SIZE);
    int readResult;
    int expectedLength;
    int messageLength;
    unsigned int userIndex;
    bool areCredentialsCorrect = false, isUserLoggedInAlready = false;
    int clientSocketDescriptor = connectionSocketDescriptors[connectionId];
    std::cout << "w watku przed petla\n";
    while (receivedMessageBuffer[0] != 'q') {

        gluedMessages.clear();
        memset(receivedMessageBuffer, 0, BUFFER_SIZE);
        std::cout << "beforeread\n";
        readResult = read(clientSocketDescriptor, receivedMessageBuffer, BUFFER_SIZE);
        if (readResult < 0) {
            fprintf(stderr, "Błąd przy próbie odczytu wiadomosci..\n");
            exit(1);
        }
        std::cout << readResult << " <- Ilosc odczytanych bajtow\n";
        std::cout << "Odczytana wiadomosc -> " << receivedMessageBuffer;
        messageLength = readResult;
        receivedMessage = receivedMessageBuffer;
        words = split(receivedMessage, " ");
        expectedLength = std::stoi(words[1]) + words[1].length();
        std::cout << expectedLength << " " << readResult << "\n";
        while (expectedLength != messageLength) {
            if (expectedLength > messageLength) {
                memset(receivedMessageBuffer, 0, BUFFER_SIZE);
                readResult = read(clientSocketDescriptor, receivedMessageBuffer, BUFFER_SIZE);
                if (readResult < 0) {
                    fprintf(stderr, "Błąd przy próbie odczytu wiadomosci..\n");
                    exit(1);
                }
                receivedMessage += receivedMessageBuffer;
                messageLength += readResult;
                if (expectedLength == messageLength) {
                    incomingMessages.push_back(receivedMessage);
                }
            }
            if (expectedLength < messageLength) {
                gluedMessages = split(receivedMessage, "\n");
                for (int i = 0; i < gluedMessages.size() - 1; i++) {
                    incomingMessages.push_back(receivedMessage);
                }
                lastOfGluedMessages = gluedMessages[gluedMessages.size() - 1];
                expectedLength = std::stoi(split(lastOfGluedMessages, "\n")[1]) - split(lastOfGluedMessages, "\n")[1].length();
                if (lastOfGluedMessages.length() == expectedLength) {
                    incomingMessages.push_back(lastOfGluedMessages);
                }
                else {
                    receivedMessage = lastOfGluedMessages;
                    messageLength = lastOfGluedMessages.length();
                }
            }
        }
        if (incomingMessages.size() == 0) incomingMessages.push_back(receivedMessage);

        while(incomingMessages.size() > 0) {
            memset(receivedMessageBuffer, 0, BUFFER_SIZE);
            memset(sentMessageBuffer, 0, BUFFER_SIZE);

            removeMessageLength(&incomingMessages[0]);
            strcpy(receivedMessageBuffer, incomingMessages[0].c_str());
            std::cout << receivedMessageBuffer << "<- przetworzona wiadomosc\n";
            incomingMessages[0] = '\0';
            incomingMessages.erase(incomingMessages.begin());
            

            if (receivedMessageBuffer[0] == 'q' || receivedMessageBuffer[0] == '0') {
                std::cout << "no to klient chyba poszedl...\n";
                break;
            }
            switch (receivedMessageBuffer[0]) {
            case 'l':
                parseLoginAndPassword(readResult - 3, receivedMessageBuffer, &login, &password);
                areCredentialsCorrect = checkIfCredentialsAreCorrectAndAddUserDataIfHeIsNew(login, password);
                userIndex = getUserIndex(login);
                isUserLoggedInAlready = checkIfUserIsLoggedInAlready(userIndex);
                connectionIdsToUserIndexesMap[connectionId] = userIndex;
                if (isUserLoggedInAlready) {
                    sendUserAlreadyLoggedInMessage(clientSocketDescriptor);
                }
                else {
                    sendResponseToClient(clientSocketDescriptor, areCredentialsCorrect);
                }
                if (areCredentialsCorrect && !isUserLoggedInAlready) {
                    userInformation[userIndex].socketDescriptor = clientSocketDescriptor;
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
                userInformation[userIndex].socketDescriptor = -1;
                connectionIdsToUserIndexesMap[userIndex] = -1;
                break;
            case 'm':
                sendMessage(receivedMessageBuffer, userIndex);
                break;
            }
        }
    }

    if (userInformation[userIndex].isOnline) {
        announceStateChange(userIndex, clientSocketDescriptor, "o ");
        setUserAsOffline(userIndex);
        userInformation[userIndex].socketDescriptor = -1;
        connectionIdsToUserIndexesMap[userIndex] = -1;
    }
    isIdBusy[connectionId] = false;
    std::cout << "klient rozlacza sie\n";
}

void Server::parseLoginAndPassword(int numberOfReadCharacters, char *message, std::string *login, std::string *password) {
    *login = "";
    *password = "";
    unsigned int iterator;
    for (iterator = 2; iterator < numberOfReadCharacters; iterator++) {
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
        writeResult = write(clientSocketDescriptor, SUCCESSFUL_LOGIN_MESSAGE, 10);
    }
    else {
        writeResult = write(clientSocketDescriptor, BAD_PASSWORD_MESSAGE, 48);
    }
    std::cout << writeResult << " <- Dlugosc odeslanej wiadomosci\n";
    if (writeResult < 0) {
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
    int messageLength = changeType.length() + userInformation[myIndex].username.length() + 3;
    std::string message = changeType + std::to_string(messageLength) + " " + userInformation[myIndex].username + "  ";
    char messageBuffer[BUFFER_SIZE];
    strcpy(messageBuffer, message.c_str());
    for (int i = 0; i < MAX_NUMBER_OF_CONCURRENT_CLIENTS; i++) {
        //std::cout << connectionIdsToUserIndexesMap[i] << "<- mapa id polaczen na indeksy uzytkownikow\n";
        if (isIdBusy[i] && connectionIdsToUserIndexesMap[i] != -1 && connectionSocketDescriptors[i] != mySocketDescriptor && userInformation[connectionIdsToUserIndexesMap[i]].isOnline) {
            writeResult = write(connectionSocketDescriptors[i], messageBuffer, message.length());
            if (writeResult < 0) {
                fprintf(stderr, "Błąd przy próbie zapisu wiadomosci..\n");
                exit(1);
            }
        }
    }
}

void Server::sendListOfOnlineUsersToClient(int clientSocketDescriptor, std::string list) {
    int writeResult = 0;
    std::string messageLength = std::to_string(list.length() + 1);
    list = list.substr(0, 2) + messageLength + " " + list.substr(2, list.length() - 2);
    char messageBuffer[list.length() + 1];
    strcpy(messageBuffer, list.c_str());
    std::cout << messageBuffer << "<- lista ludzi online\n";
    std::cout << strlen(messageBuffer) << "<- dlugosc listy\n";
    writeResult = write(clientSocketDescriptor, messageBuffer, strlen(messageBuffer));
    std::cout << messageBuffer << " <- Dlugosc odeslanej listy\n";
    if (writeResult < 0) {
        fprintf(stderr, "Błąd przy próbie zapisu wiadomosci..\n");
        exit(1);
    }
}

void Server::sendUserAlreadyLoggedInMessage(int clientSocketDescriptor) {
    int writeResult;
    char messageBuffer[30];
    memset(messageBuffer, 0, 30);
    strcpy(messageBuffer, "a 25 useralreadyloggedinrip");
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
    std::string messageLength = std::to_string(properMessage.length() - 1);
    properMessage = properMessage.substr(0, 2) + messageLength + " " + properMessage.substr(2, properMessage.length() - 3);
    strcpy(properMessageBuffer, properMessage.c_str());
    int writeResult;
    writeResult = write(userInformation[recipientIndex].socketDescriptor, properMessageBuffer, strlen(properMessageBuffer) - 1);
    std::cout << properMessageBuffer << " <- przeslana wiadomosc\n";
    std::cout << recipientNick << "<- nick odbiorcy\n";
    if (writeResult < 0) {
        fprintf(stderr, "Błąd przy próbie zapisu wiadomosci..\n");
        exit(1);
    }
}

std::vector<std::string> Server::split(std::string stringToSplit, std::string delimiter) {
    std::vector<std::string> result;
    std::string nextPart;
    unsigned int index;
    while (stringToSplit.find(delimiter) != std::string::npos) {
        index = stringToSplit.find(delimiter);
        nextPart = stringToSplit.substr(0, index);
        result.push_back(nextPart);
        stringToSplit.erase(0, index + 1);
    }
    return result;
}

void Server::removeMessageLength(std::string *str) {
    int ind = (*str).find(" ", 2);
    *str = (*str).substr(0, 2) + (*str).substr(ind + 1, (*str).length() - ind - 1);
}

Server::~Server() {

}