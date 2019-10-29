#include "User.h"
#define MAX_NUMBER_OF_CONCURRENT_CLIENTS 100
#define BUFFER_SIZE 1000
#define BAD_PASSWORD_MESSAGE "Podales niewlasciwe haslo. Sprobuj ponownie"
#define SUCCESSFUL_LOGIN_MESSAGE "Witamy"
#define LOGOUT_MESSAGE "Klient sie wylogowal"

class Server {
    public:
        //struct thread_data_t {
        //    int connectionId;
        //};

        //static const int MAX_NUMBER_OF_CONCURRENT_CLIENTS = 100;
        //static const std::string BAD_PASSWORD_MESSAGE = "Podales niewlasciwe haslo. Sprobuj ponownie";
        //static const std::string SUCCESSFUL_LOGIN_MESSAGE = "Witamy";
        struct sockaddr_in address;
        int socketDescriptor;
        int connectionSocketDescriptors[MAX_NUMBER_OF_CONCURRENT_CLIENTS];
        bool isIdBusy[MAX_NUMBER_OF_CONCURRENT_CLIENTS];

        Server(uint16_t portNumber);
        int acceptConnection();
        void threadRoutine(int connectionId);
        void handleConnection(int connectionId);
        void initialize(int connectionQueueSize);
        virtual ~Server();

    protected:

    private:
        char reuseAddressValue;
        std::vector<User> userInformation;

        int assignConnectionId();
        void parseLoginAndPassword(int numberOfReadCharacters, char *message, std::string *login, std::string *password);
        void sendResponseToClient(int clientSocketDescriptor, bool isLoginSuccessful);
        bool checkIfCredentialsAreCorrectAndAddUserDataIfHeIsNew(std::string login, std::string password);
        unsigned int getUserIndex(std::string login);
        void setUserAsOffline(unsigned int userIndex);
        void setUserAsOnline(unsigned int userIndex);
        std::string getListOfOnlineUsers(unsigned int userIndex);
        void announceStateChange(unsigned int userIndex, int clientSocketDescriptor, std::string typeOfChange);
        void sendListOfOnlineUsersToClient(int clientSocketDescriptor, std::string list);
};