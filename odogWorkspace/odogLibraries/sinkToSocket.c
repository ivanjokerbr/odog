extern void initTcp(int);
extern void connectTcp();

int pid;

void 
odog_init() {
 
    initTcp(socketPort);

    pid = fork();
    if (pid == 0)  {
       system(cmd);
       exit(0);
    }

    connectTcp();
}

void 
odog_compute() {
double *data;
    int i;
    size_t len;

    int size = odog_numberOfConnections("input");
    for(i = 0;i < size;i++) {
        while(odog_canReceive(odog_nameOfConnection("input", i), 1)) {
            odog_receive(odog_nameOfConnection("input", i), &data, &len);
            transferTcp(data);
            free(data);
        }
    }
}

void 
odog_finish() {

}
