#include "dynamicFifo.h"

void
print(dynamic_fifo *fifo) {
int i,j;

    printf("HEAD = %d %d TAIL = %d NELE = %d\n", fifo->head[CURRENT_BUCKET], fifo->head[BUCKET_INDEX],
       fifo->tail, contains_dynamic_fifo(fifo));
    for(i = 0;i < fifo->head[CURRENT_BUCKET]+1;i++) {
        printf("Bucket %d: ", i);
        for(j = 0;j < fifo->capacity;j++) {
             printf("%d ", (int)fifo->data[i][j]);
        }
        printf("\n");
    }
    printf("---\n");
}

int
main(void) {
dynamic_fifo *fifo;
int a = 1, b = 2, c = 3, d = 4, e = 5, x;

   fifo = init_dynamic_fifo(5);

   insert_dynamic_fifo(fifo, a);
   insert_dynamic_fifo(fifo, b);
   insert_dynamic_fifo(fifo, c);
   insert_dynamic_fifo(fifo, d);

   print(fifo);

   x = remove_dynamic_fifo(fifo);
   printf("removi %d\n", x);
   print(fifo);

   x = remove_dynamic_fifo(fifo);
   printf("removi %d\n", x);
   print(fifo);

   insert_dynamic_fifo(fifo, e);
   insert_dynamic_fifo(fifo, e);
   print(fifo);

   x = remove_dynamic_fifo(fifo);
   printf("removi %d\n", x);
   print(fifo);

   x = remove_dynamic_fifo(fifo);
   printf("removi %d\n", x);
   print(fifo);

   x = remove_dynamic_fifo(fifo);
   printf("removi %d\n", x);
   print(fifo);

   x = remove_dynamic_fifo(fifo);
   printf("removi %d\n", x);
   print(fifo);

   x = remove_dynamic_fifo(fifo);
   printf("removi %d\n", x);
   print(fifo);

   destroy_dynamic_fifo(fifo);

   return 0;
}
