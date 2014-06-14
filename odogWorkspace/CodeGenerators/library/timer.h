#ifndef _ODOG_TIMER
#define _ODOG_TIMER

#include <sys/time.h>
#include <time.h>

typedef struct {
  struct timeval starttime, endtime;
  struct timezone tz;
} odog_timer;

void  odog_timer_start(odog_timer *);
void  odog_timer_stop(odog_timer *);
double odog_timer_time(odog_timer *);

#endif
