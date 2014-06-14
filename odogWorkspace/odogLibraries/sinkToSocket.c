extern void initTcp(int);
extern void connectTcp();

int pid;

void 
init() {
 
    initTcp(socketPort);

    pid = fork();
    if (pid == 0)  {
       system(cmd);
       exit(0);
    }

    connectTcp();
}

void 
compute() {
double *data;
    int i;
    size_t len;

    int size = numberOfConnections("input");
    for(i = 0;i < size;i++) {
        while(canReceive(nameOfConnection("input", i), 1)) {
            receive(nameOfConnection("input", i), &data, &len);
            transferTcp(data);
            free(data);
        }
    }
}

void 
finish() {

}
