INCLUDE_DIRS = .

LIB_DIRS =

CC = gcc 
CFLAGS = -I${INCLUDE_DIRS} -g -O2 $(MACHINE_OPT)

OBJ = tcpCode.o

all: $(OBJ)
	ar cr libc-lib.a $(OBJ)
	ranlib libc-lib.a

clean:
	rm -f libc-lib.a *.o 

