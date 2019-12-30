#!/bin/bash

g++ -c User.cpp -Wall
g++ -c Server.cpp -Wall
g++ -c main.cpp -Wall
g++ -pthread -o main Server.o User.o main.o -Wall
