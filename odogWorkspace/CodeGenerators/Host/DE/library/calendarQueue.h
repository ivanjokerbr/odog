#ifndef _CALENDARQUEUE
#define _CALENDARQUEUE

#define TRUE 1
#define FALSE 0

#define MAX_NUM_ATTR 5

#define CALQSPACE 49153
#define MAXNBUCKETS 32768

typedef struct calist* calptr;

struct calist {
    double time;
    void *data;        // se for usada como uma fila global de eventos, 
                       // entao este campo eh a prioridade
    calptr next;
};

typedef struct calist calist_t;

typedef struct calendarQueue {
    calptr calq[CALQSPACE];
    // variaveis auxiliares para os metodos de processamento
    calptr *calendar;

    int calfirstsub;         // first position used in calq
    int nbuckets;
    int calqsize;            // number of events
    int lastbucket;
    int calresize_enable;

    int isGlobalQueue;      // determina qual metodo de insercao deve ser usado
    
    float caltop_threshold;
    float calbot_threshold;
    double lasttime;            //  time of the earlist event just removed    
    double buckettop;          
    double calendar_width;
} calendarQueue_t;

// FUNCTION PROTOTYPES
calendarQueue_t * calendar_newQueue(int);
void calendar_insert(calendarQueue_t *, void *, double);
void calendar_resize(calendarQueue_t *, int);
void calendar_remove(calendarQueue_t *, calptr);
void ** calendar_getEvents(calendarQueue_t *, double, int *);
void * calendar_getOneEvent(calendarQueue_t *, double);
int calendar_canReceive(calendarQueue_t *, double, int);
void calendar_printQueue(calendarQueue_t *);
void calendar_destroyQueue(calendarQueue_t *);
void calendar_insertForGlobalQueue(calendarQueue_t*, void *, double);
int calendar_size(calendarQueue_t *);
double calendar_peekNextTime(calendarQueue_t *, double);

#endif
