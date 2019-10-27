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
    std::string login, password;
    char messageBuffer[1000];
    int readResult;
    //int writeResult;
    bool areCredentialsCorrect = false;
    //struct thread_data_t *threadData = (struct thread_data_t*) threadArgs;
    int clientSocketDescriptor = connectionSocketDescriptors[connectionId];
    std::cout << "w watku przed petla\n";
    while (!areCredentialsCorrect) {
        memset(messageBuffer, 0, 1000);
        std::cout << "beforeread\n";
        readResult = read(clientSocketDescriptor, messageBuffer, 101);
        std::cout << readResult << " <- Ilosc odczytanych bajtow\n";
        if (readResult < 0) {
            //throw readingError;
            fprintf(stderr, "Błąd przy próbie odczytu wiadomosci..\n");
            exit(1);
        }
        if (messageBuffer[0] == 'q') {
            std::cout << "no to klient chyba poszedl...\n";
            
        }
        parseLoginAndPassword(readResult - 3, messageBuffer, &login, &password);
        areCredentialsCorrect = checkIfCredentialsAreCorrect(login, password);
        sendResponseToClient(clientSocketDescriptor, areCredentialsCorrect);
    }
    while (messageBuffer[0] != 'q') {
        memset(messageBuffer, 0, 1000);
        readResult = read(clientSocketDescriptor, messageBuffer, 1000);
        if (readResult < 0) {
            //throw readingError;
            fprintf(stderr, "Błąd przy próbie odczytu wiadomosci..\n");
            exit(1);
        }
        std::cout << messageBuffer;
    }

    isIdBusy[connectionId] = false;
    //delete threadData;
    std::cout << "klient rozlacza sie\n";
    //pthread_exit(NULL);
}

void Server::parseLoginAndPassword(int numberOfReadCharacters, char *message, std::string *login, std::string *password) {
    *login = "";
    *password = "";
    unsigned int iterator = 0;
    for (iterator = 0; iterator < numberOfReadCharacters; iterator++) {
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

bool Server::checkIfCredentialsAreCorrect(std::string login, std::string password) {
    for (unsigned int i = 0; i < userInformation.size(); i++) {
        if (userInformation[i].username.compare(login) == 0) {
            if (userInformation[i].password.compare(password) == 0) return true;
            return false;
        }
    }
    userInformation.push_back(User(login, password));
    return true;
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

Server::~Server() {

}