#include "User.h"
#define MAX_NUMBER_OF_CONCURRENT_CLIENTS 100
#define BAD_PASSWORD_MESSAGE "Podales niewlasciwe haslo. Sprobuj ponownie\n"
#define SUCCESSFUL_LOGIN_MESSAGE "Witamy\n"

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
        void parseLoginAndPassword(char *message, std::string *login, std::string *password);
        void sendResponseToClient(int clientSocketDescriptor, bool isLoginSuccessful);
        bool checkIfCredentialsAreCorrect(std::string login, std::string password);
};