
INCLUDE_DIRS = .

LIB_DIRS = -lpthread

CC = gcc 
CFLAGS = -I${INCLUDE_DIRS} -g -O2 $(MACHINE_OPT)

OBJ = dynamicFifoSafe.o

all: $(OBJ)
	ar cr libdynamicfifosafe.a $(OBJ)
	ranlib libdynamicfifosafe.a

clean:
	rm -f libdynamicfifosafe.a *.o 
