#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

int sockfd;
struct sockaddr_in my_addr;
struct hostent *server;

void
initTcp(int socketPort) {

    if((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
       perror("socket");
    }

    server = gethostbyname("localhost");
    bzero((char *) &my_addr, sizeof(my_addr));

    my_addr.sin_family = server->h_addrtype;
    memcpy((char *) &my_addr.sin_addr.s_addr,
         server->h_addr_list[0], server->h_length);
    my_addr.sin_port = htons(socketPort);
}

void
connectTcp( ) {
    struct timeval delay;

    delay.tv_sec = 0;
    delay.tv_usec = 10;
    while(connect(sockfd,(struct sockaddr *) &my_addr,sizeof(my_addr)) < 0) {
        select(0, (long *) 0, (long *) 0, (long *) 0, &delay);
    }
}

void
transferTcp(double *data) {
    write(sockfd, data, sizeof(double));
}




