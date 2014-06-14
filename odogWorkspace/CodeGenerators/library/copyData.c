#include <string.h>
#include "copyData.h"

odog_data_pkg *
getOdogDataPkg(void *data, size_t size) {

    odog_data_pkg *ret = (odog_data_pkg *) malloc(sizeof(odog_data_pkg));
    ret->data = (void *) malloc(size);
    memcpy(ret->data, data, size);
    ret->length = size;

    return ret;
}

