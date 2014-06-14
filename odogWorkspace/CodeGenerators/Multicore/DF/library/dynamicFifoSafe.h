#ifndef __DYNAMIC_FIFO_H
#define __DYNAMIC_FIFO_H

#include <stdio.h>
#include <pthread.h>

#define CURRENT_BUCKET 1     // which bucket to use
#define BUCKET_INDEX 0      // within a bucket, where is the pointer

struct _dynamic_fifo_safe {
    void ***data;           // dimension 1 = bucket vector, dimension 2 = vector of data within the bucket
    int head[2], tail;
    int capacity;           // the size of each bucket  
    int lastData;           // when the write and read bucket are different, this will mark the last data
   		                   // to be read on the older bucket
    pthread_mutex_t mutex;

    char readerSleeping;
    pthread_cond_t readerSleepingCond;

    short sampleRate;
};

typedef struct _dynamic_fifo_safe dynamic_fifo_safe;

dynamic_fifo_safe * init_dynamic_fifo_safe(int);
void destroy_dynamic_fifo_safe(dynamic_fifo_safe *);
void insert_dynamic_fifo_safe(dynamic_fifo_safe *, void *);
void ** remove_dynamic_fifo_safe(dynamic_fifo_safe *, int);
void canRead_dynamic_fifo_safe(dynamic_fifo_safe *, int *,
  int *, int, void (*wakeAll)(void), pthread_mutex_t *);
char isDeferrable_dynamic_fifo_safe(dynamic_fifo_safe *fifo);
void setSampleRate_dynamic_fifo_safe(dynamic_fifo_safe *fifo, int short);
short getSampleRate_dynamic_fifo_safe(dynamic_fifo_safe *fifo);

#endif
