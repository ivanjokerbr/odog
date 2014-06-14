#include "timer.h"

void 
odog_timer_start(odog_timer *t) {
  gettimeofday(&t->starttime, &t->tz);
}

void 
odog_timer_stop(odog_timer *t) {
  gettimeofday(&t->endtime, &t->tz);
}

double
odog_timer_time(odog_timer *t) {
  double ttime, start, end;

  start = (t->starttime.tv_sec + 1.0 * t->starttime.tv_usec / 1000000.0);
    end = (t->endtime.tv_sec + 1.0 * t->endtime.tv_usec / 1000000.0);
  ttime = end - start;

  return ttime;
}
