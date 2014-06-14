
INCLUDE_DIRS = .

LIB_DIRS = 

CC = gcc 
CFLAGS = -I${INCLUDE_DIRS} -g -fPIC -mtune=generic

OBJ = copyData.o \
    timer.o

main: $(OBJ)
	ar cr libodog.a $(OBJ)
	ranlib libodog.a

clean:
	rm -f libodog.a *.o 
