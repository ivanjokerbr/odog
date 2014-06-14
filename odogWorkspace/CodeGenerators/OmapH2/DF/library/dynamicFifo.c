#include "dynamicFifo.h"
#include <stdlib.h>

static void increaseCapacity(dynamic_fifo *);
static void reduceCapacity(dynamic_fifo *);

dynamic_fifo *
init_dynamic_fifo(int size) {
    dynamic_fifo *ret;

    ret = (dynamic_fifo *) malloc(sizeof(dynamic_fifo));
    ret->capacity = size;
    ret->head[CURRENT_BUCKET] = 0;
    ret->head[BUCKET_INDEX] = 0;
    ret->tail = 0;

    ret->data = (void ***) malloc(sizeof(void **));
    ret->data[0] = (void **) malloc(sizeof(void *) * size); 
    ret->lastData = -1;

    return ret;
}

void
destroy_dynamic_fifo(dynamic_fifo *fifo) {
int i;

    for(i = 0;i < fifo->head[CURRENT_BUCKET];i++) {
       free(fifo->data[i]);
    }
    free(fifo->data);
}

void
insert_dynamic_fifo(dynamic_fifo *fifo, void *data) {

    char full = willBeFull_dynamic_fifo(fifo);
    fifo->data[fifo->head[CURRENT_BUCKET]][fifo->head[BUCKET_INDEX]] = data;

    if(full == 0) {
        fifo->head[BUCKET_INDEX] = (fifo->head[BUCKET_INDEX] + 1)% fifo->capacity;
    }
    else {
        increaseCapacity(fifo);
    }

}

// It is assumed that the contains method will be allways called before te 
// remove
void **
remove_dynamic_fifo(dynamic_fifo *fifo, int nelements) {
void **ret;
int i;

    if(nelements <= 0) return NULL;

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

    return ret;
}

int 
contains_dynamic_fifo(dynamic_fifo *fifo) {
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
increaseCapacity(dynamic_fifo *fifo) {

    fifo->head[CURRENT_BUCKET]++;
    fifo->data = (void ***) realloc(fifo->data, sizeof(void **) * (fifo->head[CURRENT_BUCKET] + 1));
    fifo->data[fifo->head[CURRENT_BUCKET]] = (void **) malloc(sizeof(void *) * fifo->capacity);

    fifo->head[BUCKET_INDEX] = 0;
}

static void
reduceCapacity(dynamic_fifo *fifo) {
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

char
isEmpty_dynamic_fifo(dynamic_fifo *fifo) {
    // if the head and tail are in the same bucket, it must be the zero bucket
    if((fifo->head[CURRENT_BUCKET] == 0)  &&
       (fifo->head[BUCKET_INDEX] == fifo->tail)) {     
       return 1;
    }

    return 0;
}

char 
willBeFull_dynamic_fifo(dynamic_fifo *fifo) {
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

