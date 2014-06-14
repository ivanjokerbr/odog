
INCLUDE_DIRS = .

LIB_DIRS = 

CC = gcc 
CFLAGS = -I${INCLUDE_DIRS} -g -O2 $(MACHINE_OPT)

OBJ = copyData.o \
    timer.o

all: $(OBJ)
	ar cr libodog.a $(OBJ)
	ranlib libodog.a

clean:
	rm -f libodog.a *.o 
