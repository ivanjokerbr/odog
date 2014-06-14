
INCLUDE_DIRS = .

LIB_DIRS = 

CC = gcc 
CFLAGS = -I${INCLUDE_DIRS} -g -O2 $(MACHINE_OPT)

OBJ = dynamicFifo.o

all: $(OBJ)
	ar cr libdynamicfifo.a $(OBJ)
	ranlib libdynamicfifo.a

clean:
	rm -f libdynamicfifo.a *.o 
