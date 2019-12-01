#include<iostream>
#include<vector>
#include<string>
#include<stdio.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netdb.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <thread>

#include "Server.h"

std::mutex Server::logoutMutex;
std::mutex Server::writeMutex;
std::mutex Server::userInfoMutex;

int main() {
    int assignedId;
    Server myServer = Server(1235);
    myServer.initialize(5);
    while(1) {
        assignedId = myServer.acceptConnection();
        std::cout << "zarazrobiewatek\n";
        std::thread clientThread(&Server::threadRoutine, &myServer, assignedId);
        clientThread.detach();
    }
}