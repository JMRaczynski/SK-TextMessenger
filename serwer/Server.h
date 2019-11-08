#include "User.h"
#define MAX_NUMBER_OF_CONCURRENT_CLIENTS 100
#define BUFFER_SIZE 10000
#define BAD_PASSWORD_MESSAGE "P 46 odales niewlasciwe haslo. Sprobuj ponownie"
#define SUCCESSFUL_LOGIN_MESSAGE "W 9 itamy"
#define LOGOUT_MESSAGE "K 23 lient sie wylogowal"

class Server {
    public:
        struct sockaddr_in address;
        int socketDescriptor;
        int connectionSocketDescriptors[MAX_NUMBER_OF_CONCURRENT_CLIENTS];
        int connectionIdsToUserIndexesMap[MAX_NUMBER_OF_CONCURRENT_CLIENTS];
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
        bool checkIfUserIsLoggedInAlready(unsigned int userIndex);
        void setUserAsOffline(unsigned int userIndex);
        void setUserAsOnline(unsigned int userIndex);
        void sendUserAlreadyLoggedInMessage(int clientSocketDescriptor);
        std::string getListOfOnlineUsers(unsigned int userIndex);
        void announceStateChange(unsigned int userIndex, int clientSocketDescriptor, std::string typeOfChange);
        void sendListOfOnlineUsersToClient(int clientSocketDescriptor, std::string list);
        void sendMessage(char* messageBuffer, unsigned int userIndex);
};