#include "dynamicDspFifo.h"

char 
DynamicDspFifo_isEmpty(DynamicDspFifo *fifo) {
  return (fifo->w_ptr == fifo->r_ptr);
}

char 
DynamicDspFifo_willBeFull(DynamicDspFifo *fifo) {
    return ( (fifo->w_ptr + (fifo->step >> 1))%(fifo->capacity >> 1) == fifo->r_ptr);
}

char
DynamicDspFifo_hasRoom(DynamicDspFifo *fifo, UNSIG16 req) {
UNSIG16 r_ptr, w_ptr;

    r_ptr = fifo->r_ptr;
    w_ptr = fifo->w_ptr;

    if(w_ptr == r_ptr) {
        if(fifo->capacity/fifo->step > req) {
            return 1;
        }
        else {
            return 0;
        }
    }
    else
    if(w_ptr > r_ptr) {
        return (((fifo->capacity >> 1) - w_ptr + r_ptr)/(fifo->step >> 1) > req);
    }
    else {
        return ((r_ptr - w_ptr)/(fifo->step >> 1) > req);
    }
}

char 
DynamicDspFifo_hasData(DynamicDspFifo *fifo, UNSIG16 req) {
UNSIG16 r_ptr, w_ptr;

    r_ptr = fifo->r_ptr;
    w_ptr = fifo->w_ptr;

    if(w_ptr == r_ptr) { 
        return 0;
    }
    else
    if(w_ptr > r_ptr) {
        return ((w_ptr - r_ptr)/(fifo->step >> 1) >= req);
    }
    else {
        return (((fifo->capacity >> 1)- r_ptr + w_ptr)/(fifo->step >> 1) >= req);
    }
}

void
DynamicDspFifo_insertData(DynamicDspFifo *fifo, UNSIG16 *dataSection, UNSIG16 *data) {

#ifdef __ARM__
    memcpy(dataSection + fifo->w_ptr, data, fifo->step);
#else
    memcpy(dataSection + fifo->w_ptr, data, fifo->step >> 1);
#endif
    fifo->w_ptr = (fifo->w_ptr + (fifo->step >> 1)) % (fifo->capacity >> 1);
}

UNSIG16 *
DynamicDspFifo_remove(DynamicDspFifo *fifo, UNSIG16 *dataSection) {
UNSIG16 *data;

    data = dataSection + fifo->r_ptr;
    fifo->r_ptr = (fifo->r_ptr + (fifo->step >> 1)) % (fifo->capacity >> 1);
    return data;
}


