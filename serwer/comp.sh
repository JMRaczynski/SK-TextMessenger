#!/bin/bash

g++ -c User.cpp
g++ -c Server.cpp
g++ -c main.cpp
g++ -pthread -o main Server.o User.o main.o -Wall
