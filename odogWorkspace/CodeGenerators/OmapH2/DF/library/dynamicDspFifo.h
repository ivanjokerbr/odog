#ifndef _DYNAMIC_DSP_FIFO__H
#define _DYNAMIC_DSP_FIFO__H

#ifdef __ARM__
#define UNSIG16 unsigned short
#else
#define UNSIG16 unsigned int
#endif

struct _dynamicDspFifo {
   UNSIG16 w_ptr;
   UNSIG16 r_ptr;
   UNSIG16 capacity;
   UNSIG16 step;
};

typedef struct _dynamicDspFifo DynamicDspFifo;
char DynamicDspFifo_isEmpty(DynamicDspFifo *fifo);
char DynamicDspFifo_willBeFull(DynamicDspFifo *fifo);
char DynamicDspFifo_hasRoom(DynamicDspFifo *fifo, UNSIG16 req);
char DynamicDspFifo_hasData(DynamicDspFifo *fifo, UNSIG16 req);
void DynamicDspFifo_insertData(DynamicDspFifo *fifo, UNSIG16 *dataSection, UNSIG16 *data);
UNSIG16 *DynamicDspFifo_remove(DynamicDspFifo *fifo, UNSIG16 *dataSection);

#endif

