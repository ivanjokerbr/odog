
INCLUDE_DIRS = .

LIB_DIRS = 

CC = gcc 
CFLAGS = -I${INCLUDE_DIRS} -O3 -mtune=generic -D__ARM__

OBJ = dynamicFifo.o 

OBJ2 = dynamicDspFifo.o

main: $(OBJ) $(OBJ2)
	ar cr libdynamicfifo.a $(OBJ)
	ranlib libdynamicfifo.a
	ar cr libdynamicDSPFifo.a $(OBJ2)
	ranlib libdynamicDSPFifo.a

clean:
	rm -f libdynamicfifo.a *.o 



