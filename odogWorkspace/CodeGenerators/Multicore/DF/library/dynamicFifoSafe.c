#include "dynamicFifoSafe.h"
#include <stdlib.h>
#include <pthread.h>

static void increaseCapacity(dynamic_fifo_safe *);
static void reduceCapacity(dynamic_fifo_safe *);
static char isEmpty_dynamic_fifo(dynamic_fifo_safe *);
static char willBeFull_dynamic_fifo(dynamic_fifo_safe *);
static int numberOfElements(dynamic_fifo_safe *fifo);

dynamic_fifo_safe *
init_dynamic_fifo_safe(int size) {
    dynamic_fifo_safe *ret;

    ret = (dynamic_fifo_safe *) malloc(sizeof(dynamic_fifo_safe));
    ret->capacity = size;
    ret->head[CURRENT_BUCKET] = 0;
    ret->head[BUCKET_INDEX] = 0;
    ret->tail = 0;

    ret->data = (void ***) malloc(sizeof(void **));
    ret->data[0] = (void **) malloc(sizeof(void *) * size); 
    ret->lastData = -1;

    pthread_mutex_init(&ret->mutex, NULL);
 
    ret->readerSleeping = 0;
    pthread_cond_init(&ret->readerSleepingCond, NULL);

    return ret;
}

void
destroy_dynamic_fifo_safe(dynamic_fifo_safe *fifo) {
int i, j;

    for(i = 0;i < fifo->head[CURRENT_BUCKET];i++) {      
       for(j = 0;j < fifo->capacity;j++) {
           free(fifo->data[i][j]);
       }
       free(fifo->data[i]);
    }

    j = fifo->tail;
    while(j != fifo->head[BUCKET_INDEX]) {
        free(fifo->data[i][j]);
        j = (j + 1) % fifo->capacity;
    }
    free(fifo->data[fifo->head[CURRENT_BUCKET]]);
    free(fifo->data);

    pthread_mutex_destroy(&fifo->mutex);
    pthread_cond_destroy(&fifo->readerSleepingCond);

    free(fifo);
}

void
insert_dynamic_fifo_safe(dynamic_fifo_safe *fifo, void *data) {

    pthread_mutex_lock(&fifo->mutex);

    char full = willBeFull_dynamic_fifo(fifo);
    fifo->data[fifo->head[CURRENT_BUCKET]][fifo->head[BUCKET_INDEX]] = data;

    if(full == 0) {
        fifo->head[BUCKET_INDEX] = (fifo->head[BUCKET_INDEX] + 1)% fifo->capacity;
    }
    else {
        increaseCapacity(fifo);
    }

    if(fifo->readerSleeping == 1) {
        pthread_cond_signal(&fifo->readerSleepingCond);
    }
    pthread_mutex_unlock(&fifo->mutex);
}

// It is assumed that the contains method will be allways called before te 
// remove
void **
remove_dynamic_fifo_safe(dynamic_fifo_safe *fifo, int nelements) {
void **ret;
int i;

    if(nelements <= 0) return NULL;

    pthread_mutex_lock(&fifo->mutex);

    if(fifo->head[CURRENT_BUCKET] != 0 &&
           (fifo->lastData == -1)) {
        fifo->lastData = fifo->tail;
    }

    ret = (void **) malloc(sizeof(void *) * nelements);

    for(i = 0;i < nelements;i++) {
        ret[i] = fifo->data[0][fifo->tail];
        fifo->tail = (fifo->tail + 1) % fifo->capacity;   
        if(fifo->tail == fifo->lastData) {
            reduceCapacity(fifo);
        }  
    }

    pthread_mutex_unlock(&fifo->mutex);

    return ret;
}

// ensures that data is available, otherwise blocks
void
canRead_dynamic_fifo_safe(dynamic_fifo_safe *fifo, int *numberOfWriteBlocked,
     int *numberOfReadBlocked, int ncomponents, void (*wakeAll)(void), 
     pthread_mutex_t *stateMutex) {
int nele = 0;

    pthread_mutex_lock(&fifo->mutex);

    nele = numberOfElements(fifo);
    if(nele >= fifo->sampleRate) {
        pthread_mutex_unlock(&fifo->mutex);
        return;
    }

    pthread_mutex_lock(stateMutex);
    if(*numberOfWriteBlocked + *numberOfReadBlocked + 1 == ncomponents) {
        wakeAll();
    }
    (*numberOfReadBlocked)++;
    pthread_mutex_unlock(stateMutex);

    fifo->readerSleeping = 1;
    while(numberOfElements(fifo) < fifo->sampleRate) {
        pthread_cond_wait(&fifo->readerSleepingCond, &fifo->mutex);
    }
    fifo->readerSleeping = 0;

    pthread_mutex_lock(stateMutex);
    (*numberOfReadBlocked)--;
    pthread_mutex_unlock(stateMutex);

    pthread_mutex_unlock(&fifo->mutex);
}

char
isDeferrable_dynamic_fifo_safe(dynamic_fifo_safe *fifo) {

    pthread_mutex_lock(&fifo->mutex);

    int nele = numberOfElements(fifo);
    if(nele >= fifo->sampleRate) {
         pthread_mutex_unlock(&fifo->mutex);
         return 1;
    }

    pthread_mutex_unlock(&fifo->mutex);
    return 0;
}

void
setSampleRate_dynamic_fifo_safe(dynamic_fifo_safe *fifo, short rate) {

    pthread_mutex_lock(&fifo->mutex);

    fifo->sampleRate = rate;

    pthread_mutex_unlock(&fifo->mutex);
}

short
getSampleRate_dynamic_fifo_safe(dynamic_fifo_safe *fifo) {
short rate;

    pthread_mutex_lock(&fifo->mutex);

    rate = fifo->sampleRate;

    pthread_mutex_unlock(&fifo->mutex);

    return rate;
}

///////////////////////////////////////////// LOCAL FUNCTIONS

static int
numberOfElements(dynamic_fifo_safe *fifo) {
int nele = 0;

    if(fifo->head[CURRENT_BUCKET] != 0) {
         nele = fifo->head[CURRENT_BUCKET] * fifo->capacity + 
             fifo->head[BUCKET_INDEX];
         if(fifo->lastData != -1) {
             if(fifo->tail < fifo->lastData) {
                 nele -= fifo->lastData - fifo->tail;
             }  
             else {
                 nele -= fifo->tail - fifo->lastData;
             }
         }
    }
    else
    if(fifo->head[BUCKET_INDEX] > fifo->tail) {
        nele = fifo->head[BUCKET_INDEX] - fifo->tail;
    }
    else
    if(fifo->head[BUCKET_INDEX] == fifo->tail) {
        return 0;
    }
    else {
        nele = fifo->head[BUCKET_INDEX] + fifo->capacity - fifo->tail;
    }

    return nele;
}

// the number of extra buckets to add
static void
increaseCapacity(dynamic_fifo_safe *fifo) {

    fifo->head[CURRENT_BUCKET]++;
    fifo->data = (void ***) realloc(fifo->data, sizeof(void **) * (fifo->head[CURRENT_BUCKET] + 1));
    fifo->data[fifo->head[CURRENT_BUCKET]] = (void **) malloc(sizeof(void *) * fifo->capacity);

    fifo->head[BUCKET_INDEX] = 0;
}

static void
reduceCapacity(dynamic_fifo_safe *fifo) {
int i;

    free(fifo->data[0]);
    for(i = 0;i < fifo->head[CURRENT_BUCKET];i++) {
        fifo->data[i] = fifo->data[i+1];
    }
    fifo->head[CURRENT_BUCKET]--;
    fifo->data = (void ***) realloc(fifo->data, sizeof(void **) * (fifo->head[CURRENT_BUCKET] + 1));

    fifo->tail = 0;
    fifo->lastData = -1;
}

static char
isEmpty_dynamic_fifo(dynamic_fifo_safe *fifo) {
    // if the head and tail are in the same bucket, it must be the zero bucket
    if((fifo->head[CURRENT_BUCKET] == 0)  &&
       (fifo->head[BUCKET_INDEX] == fifo->tail)) {     
       return 1;
    }

    return 0;
}

static char 
willBeFull_dynamic_fifo(dynamic_fifo_safe *fifo) {
    if(fifo->head[CURRENT_BUCKET] == 0 && 
        (fifo->head[BUCKET_INDEX] + 1)%fifo->capacity == 
        fifo->tail)
        return 1;
    else
    if(fifo->head[CURRENT_BUCKET] != 0 && 
       fifo->head[BUCKET_INDEX] == fifo->capacity - 1) 
        return 1;
        
    return 0;
}

