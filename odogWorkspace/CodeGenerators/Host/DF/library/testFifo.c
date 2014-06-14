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
int a = 1, b = 2, c = 3, d = 4, e = 5;
void **x;

   fifo = init_dynamic_fifo(2);

   insert_dynamic_fifo(fifo, a);
   print(fifo);

   insert_dynamic_fifo(fifo, b);

   print(fifo);
   insert_dynamic_fifo(fifo, b);

   print(fifo);
   insert_dynamic_fifo(fifo, c);

   print(fifo);

   x = remove_dynamic_fifo(fifo, 1);
   printf("removi %d\n", x[0]);
   print(fifo);

   x = remove_dynamic_fifo(fifo, 1);
   printf("removi %d\n", x[0]);
   print(fifo);

   insert_dynamic_fifo(fifo, d);
   insert_dynamic_fifo(fifo, e);

   print(fifo);

   x = remove_dynamic_fifo(fifo, 1);
   printf("removi %d\n", x[0]);
   print(fifo);

   x = remove_dynamic_fifo(fifo, 1);
   printf("removi %d\n", x[0]);
   print(fifo);

   x = remove_dynamic_fifo(fifo, 1);
   printf("removi %d\n", x[0]);
   print(fifo);

   x = remove_dynamic_fifo(fifo, 1);
   printf("removi %d\n", x[0]);
   print(fifo);

   x = remove_dynamic_fifo(fifo, 1);
   printf("removi %d\n", x[0]);
   print(fifo);

   destroy_dynamic_fifo(fifo);

   return 0;
}
