#ifndef __DYNAMIC_FIFO_H
#define __DYNAMIC_FIFO_H

#include <stdio.h>

#define CURRENT_BUCKET 1     // which bucket to use
#define BUCKET_INDEX 0      // within a bucket, where is the pointer

struct _dynamic_fifo {
    void ***data;           // dimension 1 = bucket vector, dimension 2 = vector of data within the bucket
    int head[2], tail;
    int capacity;           // the size of each bucket  
    int lastData;           // when the write and read bucket are different, this will mark the last data
		            // to be read on the older bucket
};

typedef struct _dynamic_fifo dynamic_fifo;

dynamic_fifo * init_dynamic_fifo(int);
void destroy_dynamic_fifo(dynamic_fifo *);
void insert_dynamic_fifo(dynamic_fifo *, void *);
void ** remove_dynamic_fifo(dynamic_fifo *, int);
char isEmpty_dynamic_fifo(dynamic_fifo *);
char willBeFull_dynamic_fifo(dynamic_fifo *);
int contains_dynamic_fifo(dynamic_fifo *);
#endif
