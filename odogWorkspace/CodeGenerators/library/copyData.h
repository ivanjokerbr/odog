#ifndef _COPYDATA__H
#define _COPYDATA__H
#include <stdlib.h>

struct _odog_data_pkg {
    void *data;
    int length;
};

typedef struct _odog_data_pkg odog_data_pkg;

odog_data_pkg *getOdogDataPkg(void *, size_t);

#endif
