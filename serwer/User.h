#include<string>

class User {
    public:
        std::string username;
        std::string password;
        bool isOnline;

        User(std::string name, std::string password);
        virtual ~User();
    protected:

    private:

};