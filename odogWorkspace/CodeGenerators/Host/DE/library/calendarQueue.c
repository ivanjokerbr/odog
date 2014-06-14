/*************************************************************************/ 
/** */ 
/** Calendar Queueing Method for Future Event List Manipulation: */ 
/** As presented in the article by Randy Brown in Communications of */ 
/** the ACM, Oct. 1988 Vol. 30 Num. 10. Coded by Eric Callman 3/4/92 */
// 
// Modified by Ivan Jeukens, June 2007. 
//

#include <stdio.h>
#include <malloc.h>
#include <float.h>

#include "calendarQueue.h"

// LOCAIS
static void releaseEntry(calendarQueue_t *, calptr);
static void initQueue(calendarQueue_t *, int, int, double, double);
static void resize(calendarQueue_t *, int);
static double new_cal_width(calendarQueue_t *);

int calendar_size(calendarQueue_t *queue) {    
    return queue->calqsize;
}

void
calendar_destroyQueue(calendarQueue_t *queue) {
    int i;
    calptr tmp, tmp2;

    queue->calresize_enable = FALSE;
    for(i = 0;i < queue->nbuckets;i++) {
        tmp = queue->calendar[i];
        while(tmp != NULL) {
            tmp2 = tmp;
            tmp = tmp->next;
            free(tmp2->data); 
            free(tmp2);
        }

    }
    free(queue);
}

/*************************************************************************/
/*                                                                       */
/* calendar_init:                                                        */
/*     This routine uses calendar_localinit() to start the calendar with */
/* nothing in it for initializing.                                       */
/*                                                                       */
/*************************************************************************/

calendarQueue_t *
calendar_newQueue(int isGlobalQueue)
{
calendarQueue_t *queue;

   queue = (calendarQueue_t *) malloc(sizeof(calendarQueue_t));
   queue->isGlobalQueue = isGlobalQueue;
   initQueue(queue, 0, 2, 1.0, 0.0);
   queue->calresize_enable = TRUE;
   
   return queue;
}

// Determina se ha algum evento na fila que o mesmo timestamp que indicado no parametro
int
calendar_canReceive(calendarQueue_t *queue, double timestamp, int nevents)  {
    int index;
    char achou = 0;
    calptr tmp;

    if(nevents <= 0) return FALSE;
    if(queue->calqsize == 0) return FALSE;

    index = queue->lastbucket;
    do {
        if(queue->calendar[index] != NULL && 
                queue->calendar[index]->time == timestamp) {
            achou = 1;
            nevents--;
            break;
        }
        
        index++;
        if(index == queue->nbuckets) {
            index = 0;
        }
    } while(index != queue->lastbucket);

    if(achou == 0) return FALSE;
    if(nevents == 0) return TRUE;

    tmp = queue->calendar[index]->next;
    while(tmp != NULL && tmp->time == timestamp && nevents > 0) {
        nevents--;
        tmp = tmp->next;
    }
    
    if(nevents == 0) return TRUE;

    return FALSE;
}

// Remove todos os eventos que tenham o timestamp indicado.
// O valor de retorno eh um vetor dos ponteiros de dados.
// O parametro nevents indica quantos eventos foram lidos.
//
// A implementacao deste metodo leva em conta seu uso: sera chamado por um escalonador
// que antes ira testar a existencia de eventos com o metodo canReceive. Desta forma,
// o timestamp especificado sera sempre o menor possivel, ou seja, nunca sera
// necessario retirar eventos da fila, sabendo que ha outros nela com menor timestamp.
// Entretanto, eh necessario ter cuidado pois o usuario tambem chama estes metodos
// em seus atores. Assim, alguem descuidado pode chamar esse metodo sem antes
// ter testado a existencia de eventos.
void **
calendar_getEvents(calendarQueue_t *queue, double timestamp, int *nevents) {
    int achou = FALSE;
    calptr tmp, tmp2;
    calptr listret = NULL, event;
    void **ret;
    int index;
    
    *nevents = 0;

    if(queue->calqsize == 0) return NULL;

    // 1. procura pelo bucket com o evento
    index = queue->lastbucket;
    do {
        if(queue->calendar[index] != NULL && 
            queue->calendar[index]->time == timestamp) {
            achou = TRUE;
            queue->lastbucket = index;
            queue->lasttime = timestamp;
            break;
        }
        
        index++;
        if(index == queue->nbuckets) {
            index = 0;
        }

        queue->buckettop += queue->calendar_width;
    } while(index != queue->lastbucket);

    if(achou == FALSE) {
        return NULL;
    }

    // 2. percorre a lista encadeada do bucket encontrado, retirando todos
    // os eventos com os timestamps indicados
    listret = (calist_t *) malloc(sizeof(calist_t));
    listret->data = queue->calendar[index]->data;
    listret->next = NULL;
    *nevents = 1;

    tmp = queue->calendar[index]->next;
    tmp2 = queue->calendar[index];
    queue->calendar[index] = tmp;
    releaseEntry(queue, tmp2);

    while(tmp != NULL && tmp->time == timestamp) {
        event = (calist_t *) malloc(sizeof(calist_t));
        event->data = tmp->data;

        event->next = listret->next;
        listret->next = event;
        (*nevents)++;

        tmp2 = tmp;
        tmp = tmp->next;

        queue->calendar[index] = tmp;
        releaseEntry(queue, tmp2);
    }

    ret = (void **) malloc(sizeof(void *) * (*nevents));
    index = 0;
    tmp = listret;
    while(tmp != NULL) {
        ret[index] = tmp->data;
        index++;
        tmp2 = tmp;
        tmp = tmp->next;

        free(tmp2);
    }

    return ret;
}

void *
calendar_getOneEvent(calendarQueue_t *queue, double timestamp) {
    int achou = FALSE;
    calptr tmp, tmp2;
    calptr listret = NULL, event;
    int index;
    void *ret;
   
    if(queue->calqsize == 0) return NULL;

    // 1. procura pelo bucket com o evento
    index = queue->lastbucket;
    do {
        if(queue->calendar[index] != NULL && 
            queue->calendar[index]->time == timestamp) {
            achou = TRUE;
            queue->lastbucket = index;
            queue->lasttime = timestamp;
            break;
        }
        
        index++;
        if(index == queue->nbuckets) {
            index = 0;
        }

        queue->buckettop += queue->calendar_width;
    } while(index != queue->lastbucket);

    if(achou == FALSE) {
        return NULL;
    }

    // 2. percorre a lista encadeada do bucket encontrado, retirando todos
    // os eventos com os timestamps indicados
    ret = queue->calendar[index]->data;

    tmp = queue->calendar[index]->next;
    tmp2 = queue->calendar[index];
    queue->calendar[index] = tmp;
    releaseEntry(queue, tmp2);

    return ret;
}

void
calendar_insert(calendarQueue_t *queue, void *data, double timestap) {
   int i;
   calptr temp, traverse;

   /* calculate the number of the bucket to place new entry in */
   i = (int)(timestap / (float) queue->calendar_width);    /* find virtual bucket */
   i = i % queue->nbuckets;                                /* find actual bucket */

   temp = (calist_t *) malloc(sizeof(*temp)); /* initialize a list node */
   temp->time = timestap;
   temp->data = data;
   
   traverse = queue->calendar[i];     /* grab head of list of events in that bucket */
                                       /* put in head of list if appropriate */
   if ((traverse == NULL) || (traverse->time > temp->time)) {
      temp->next = traverse;
      queue->calendar[i] = temp;
   } 
   else {
      while ((traverse->next != NULL) &&    /* find the correct spot in list */
             (traverse->next->time < temp->time)) {
         traverse = traverse->next;
      }
      temp->next = traverse->next;                 /* put in the new element */
      traverse->next = temp;
   }

   queue->calqsize++;                               /* update the size of the queue */
   if ((queue->calqsize > queue->caltop_threshold) && (queue->nbuckets < MAXNBUCKETS)) {
       resize(queue, 2 * queue->nbuckets);      /* double the size of the calendar */
   }
}

// data tem que ser um ponteiro para inteiro previamente allocado
void
calendar_insertForGlobalQueue(calendarQueue_t *queue, void *data, double timestamp) {
   int i, *var;
   calptr temp, traverse;
   
   /* calculate the number of the bucket to place new entry in */
   i = (int)(timestamp / (float) queue->calendar_width);    /* find virtual bucket */
   i = i % queue->nbuckets;                                /* find actual bucket */
   
   temp = (calist_t *) malloc(sizeof(*temp)); /* initialize a list node */
   temp->time = timestamp;
   temp->data = data;
   
   traverse = queue->calendar[i];
   if ((traverse == NULL) || (traverse->time > temp->time)) {
      temp->next = traverse;
      queue->calendar[i] = temp;
   } 
   else
   if ((traverse != NULL) && (traverse->time == timestamp)) {
        free(temp);
        if(*((int *)traverse->data) > *((int *)data)) {
          var = (int *) traverse->data;
          traverse->data = data;
          free(var);
          return;
        }
        else {
          free(data);
          return;
        }
   }
   else {
      while((traverse->next != NULL) &&
             (traverse->next->time < temp->time)) {
         traverse = traverse->next;
      }

      if((traverse->next == NULL) || (traverse->next->time > temp->time)) {
          temp->next = traverse->next;
          traverse->next = temp;
      }
      else { // o campo time eh igual
          free(temp);
          if(*((int *)traverse->next->data) > *((int *)data)) {
              var = (int *) traverse->next->data;
              traverse->next->data = data;
              free(var);
              return;
          }
          else {
              free(data);
              return;
          }
      }
   }

   queue->calqsize++;                               /* update the size of the queue */
   if ((queue->calqsize > queue->caltop_threshold) && (queue->nbuckets < MAXNBUCKETS)) {
       resize(queue, 2 * queue->nbuckets);      /* double the size of the calendar */
   }
}

double
calendar_peekNextTime(calendarQueue_t *queue, double currentTime) {
double tmp = DBL_MAX;
int index;

    if(queue->calqsize == 0) return -1;

    index = queue->lastbucket;
    do {
        if((queue->calendar[index] != NULL) && 
           (queue->calendar[index]->time > currentTime) &&
           (queue->calendar[index]->time < tmp)) {
              tmp = queue->calendar[index]->time;
        }
        index++;
        if(index == queue->nbuckets) {
            index = 0;
        }
    } while(index != queue->lastbucket);

    if(tmp == DBL_MAX) return -1;
    
    return tmp;
}

/*************************************************************************/
/*                                                                       */
/* calendar_remove:                                                      */
/*     This function removes the next item from the calendar queue, and  */
/* returnes that item in the given parameter.                            */
/*                                                                       */
/*************************************************************************/
// o parametro ent tem que estar associado a uma posicao calist_t ja alocada
void
calendar_remove(calendarQueue_t *queue, calptr ent) {
   register int i;
   int flag, temp2;
   calptr temp;
   float lowest;

   i = queue->lastbucket;
   flag = 0;
   while (flag == 0) {
      if(ent == NULL) printf("No return entry!\n");
      
      if((queue->calendar[i] != NULL) && 
             (queue->calendar[i]->time < queue->buckettop)) {
	      ent->time = queue->calendar[i]->time;
	      ent->data = queue->calendar[i]->data;

          temp = queue->calendar[i];
          queue->calendar[i] = queue->calendar[i]->next;
	      free(temp);

	      queue->lastbucket = i;
	      queue->lasttime = ent->time;
	      flag = 1;                         // stops the search

	      queue->calqsize--;
	      if(queue->calqsize < queue->calbot_threshold) {
	          resize(queue, (int)((float)queue->nbuckets / 2));
          }
      }
      else {
          i++; 
          if(i == queue->nbuckets) {
              i = 0;
          }
          queue->buckettop += queue->calendar_width;
          if(i == queue->lastbucket) {
              flag = 2;                  // not on the first position of a bucket
          }
      }
   }

   if(flag == 2) {
       temp2 = -1;
       for(i = 0; i < queue->nbuckets; i++) {
          if((queue->calendar[i] != NULL) && ((temp2 == -1) ||
                 (queue->calendar[i]->time < lowest))) {
              temp2 = i;
              lowest = queue->calendar[i]->time;
          }
       }
       ent->time = queue->calendar[temp2]->time;
       ent->data = queue->calendar[temp2]->data;

       temp = queue->calendar[temp2];
       queue->calendar[temp2] = queue->calendar[temp2]->next;
       free(temp);

       queue->lastbucket = temp2;
       queue->lasttime = ent->time;
       queue->buckettop = (float)((int)(queue->lasttime / queue->calendar_width) + 1) *
           queue->calendar_width + (0.5 * queue->calendar_width);
       queue->calqsize--;
       if (queue->calqsize < queue->calbot_threshold) {
           resize(queue, (int)((float) queue->nbuckets / 2));
       }
   }
}

void
calendar_printQueue(calendarQueue_t *queue) {
   int i;
   calptr temp;

   for(i = 0; i < queue->nbuckets; i++) {
      temp = queue->calendar[i];
      if (i == queue->lastbucket)
	      printf("bucket %d (last) -> ", i);
      else 
	      printf("bucket %d -> ",i);

      while (temp != NULL){
          if(queue->isGlobalQueue) {
              printf("%f %d ", temp->time, *((int *)temp->data));
          }
          else {
	          printf("%f ", temp->time);
          }
	      temp = temp->next;
      }
      printf("\n");
   }

}

// USO LOCAL
static void
releaseEntry(calendarQueue_t *queue, calptr ent) {
    if(queue->isGlobalQueue) {
        free((int*)ent->data);
    }
    free(ent);
    queue->calqsize--;
    if (queue->calqsize < queue->calbot_threshold) {
        resize(queue, (int)((float) queue->nbuckets / 2));
    }
}

/*************************************************************************/
/*                                                                       */
/* calendar_localinit:                                                   */
/*     This routine initializes a bucket array within the array calq[].  */
/* Calendar[0] is made equal to calq[qbase], the number of buckets is    */
/* nbuck, and startprio is the priority at which dequeing begins.  All   */
/* external variables except calresize_enable are initialized.           */
/*                                                                       */
/*************************************************************************/
static void
initQueue(calendarQueue_t *queue, int qbase, int nbuck, double bwidth,
    double starttime) {
   int i;
   long n;

   queue->calfirstsub = qbase;
   queue->calendar = queue->calq + qbase;
   queue->calendar_width = bwidth;
   queue->nbuckets = nbuck;

   queue->calqsize = 0;
   for (i = 0; i < nbuck; i++) {
       queue->calendar[i] = NULL;
   }
   queue->lasttime = starttime;
   n = (long)((float) starttime / bwidth);
   queue->lastbucket = n % nbuck;
   queue->buckettop = (float)(n + 1) * bwidth + (0.5 * bwidth);

   queue->calbot_threshold = (int)((float)nbuck / 2) - 2;
   queue->caltop_threshold = 2 * nbuck;
}

static void
resize(calendarQueue_t *queue, int newsize)
{
   double bwidth;
   int i, oldnbuckets;
   calptr *oldcalendar, temp, temp2;
   
   if(!queue->calresize_enable) return;

   bwidth = new_cal_width(queue);
   oldcalendar = queue->calendar;
   oldnbuckets = queue->nbuckets;

   if(queue->calfirstsub == 0)  {
      initQueue(queue, CALQSPACE-newsize, newsize, bwidth, queue->lasttime);
   }
   else {
      initQueue(queue, 0, newsize, bwidth, queue->lasttime);
   }

   for(i = oldnbuckets-1; i >= 0; i--) {
      temp = oldcalendar[i];
      while (temp != NULL) {
          temp2 = temp;
          if(queue->isGlobalQueue) {
              calendar_insertForGlobalQueue(queue, temp->data, temp->time);
          }
          else {
	          calendar_insert(queue, temp->data, temp->time);
          }
	      temp = temp->next;

          /*if(queue->isGlobalQueue) {
              free(temp2->data);
          }*/
          free(temp2);
      }
   }
}

/*************************************************************************/
/*                                                                       */
/* new_cal_width:                                                        */
/*     This function returns the width that the buckets should have      */
/* based on a random sample of the queue so that there will be about 3   */
/* items in each bucket.                                                 */
/*                                                                       */
/*************************************************************************/
static double 
new_cal_width(calendarQueue_t *queue)
{
   int nsamples, templastbucket, i, j, *prio;
   float templasttime;
   double tempbuckettop, average, newaverage;
   calist_t temp[25];

   if(queue->calqsize < 2) return(1.0);

   if(queue->calqsize <= 5) {
       nsamples = queue->calqsize;
   }
   else {
       nsamples = 5 + (int)((float) queue->calqsize / 10);
   }

   if (nsamples > 25) nsamples = 25;

   templastbucket = queue->lastbucket;
   templasttime = queue->lasttime;
   tempbuckettop = queue->buckettop;

   queue->calresize_enable = FALSE;

   average = 0.0;
   for(i = 0; i < nsamples; i++) {
      calendar_remove(queue, &temp[i]);
      if (i > 0) {
          average += temp[i].time - temp[i-1].time;
      }
   }
   
   average = average / (float)(nsamples - 1);
   newaverage = 0.0; 
   j = 0;
   
   if(queue->isGlobalQueue) {
       calendar_insertForGlobalQueue(queue, temp[0].data, temp[0].time);
   }
   else {
       calendar_insert(queue, temp[0].data, temp[0].time);
   }
   
   for (i = 1; i < nsamples; i++){
      if((temp[i].time - temp[i-1].time) < (average * 2.0)) {
	     newaverage += (temp[i].time - temp[i-1].time);
	     j++;
      }
      if(queue->isGlobalQueue) {
          calendar_insertForGlobalQueue(queue, temp[i].data, temp[i].time);
      }
      else {
          calendar_insert(queue, temp[i].data, temp[i].time);
      }
   }
   newaverage = (newaverage / (float)j) * 3.0;

   queue->lastbucket = templastbucket;
   queue->lasttime = templasttime;
   queue->buckettop = tempbuckettop;
   queue->calresize_enable = TRUE;

   return (newaverage);
}

