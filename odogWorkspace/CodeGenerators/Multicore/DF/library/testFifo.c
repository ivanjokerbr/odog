#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>

#include "dynamicFifoSafe.h"

int  thread_ids[2] = {0,1};
dynamic_fifo_safe *con1;
dynamic_fifo_safe *con2;

void
printsafe(char *string) {
    write(0, string, strlen(string));
}

void *
writer(void *idp) {
double *d;
int count = 0;

    while(count < 50) {
        printsafe("tou no inicio...\n");

_yieldTest:

        if(isDeferrable_dynamic_fifo_safe(con1) == 0) goto _execute;
        if(isDeferrable_dynamic_fifo_safe(con1) == 0) goto _execute;

        printsafe("w yield\n");
        pthread_yield();
        goto _yieldTest;

_execute:
        printsafe("w writing\n");

        d = (double *) malloc(sizeof(double));
        *d = 10.5;
        insert_dynamic_fifo_safe(con1, d);

        printsafe("inseri1\n");

        d = (double *) malloc(sizeof(double));
        *d = 69.69;
        insert_dynamic_fifo_safe(con2, d);

        printsafe("inseri 2\n");

        count++;        
    }
    printsafe("writer finished\n");
}

void  *
reader(void *idp) {
double **d, **x;
int count = 0;
char str[100];

    while(count < 25) {
       printsafe("cr con1\n");
       canRead_dynamic_fifo_safe(con1);
       printsafe("cr con2\n");
       canRead_dynamic_fifo_safe(con2);

       d = (double **) remove_dynamic_fifo_safe(con1, 2);
       x = (double **) remove_dynamic_fifo_safe(con2, 1);

       sprintf(str, "d0 = %f d1 = %f x = %f\n", *d[0], *d[1], *x[0]);
       printsafe(str);
  
       free(d[0]);
       free(d[1]);
       free(x[0]);
       free(d);
       free(x);

       count++;
    }

    printsafe("reader finished\n");
}

int
main(void) {
pthread_t components[2];
pthread_attr_t attr;

   // no composite init
    con1 = init_dynamic_fifo_safe(30);
    con2 = init_dynamic_fifo_safe(15);
    
    // nos respectivos comps
    setSampleRate_dynamic_fifo_safe(con1, 2);
    setSampleRate_dynamic_fifo_safe(con2, 1);

   // no composite compute
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
    pthread_create(&components[0], &attr, writer, (void *)&thread_ids[0]);
    pthread_create(&components[1], &attr, reader, (void *)&thread_ids[1]);
    

    // no composite finish
    pthread_join(components[0], NULL);
    pthread_join(components[1], NULL);

    pthread_attr_destroy(&attr);

    destroy_dynamic_fifo_safe(con1);
    destroy_dynamic_fifo_safe(con2);
   
    return 0;
}
