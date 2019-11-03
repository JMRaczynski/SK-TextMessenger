#include "User.h"
#include <string>

User::User(std::string name, std::string pw) {
    username = name;
    password = pw;
    isOnline = false;
}

User::~User() {

}