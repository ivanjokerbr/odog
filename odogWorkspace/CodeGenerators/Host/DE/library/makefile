
INCLUDE_DIRS = .

LIB_DIRS = 

CC = gcc 
CFLAGS = -I${INCLUDE_DIRS} -g -O2 $(MACHINE_OPT)

OBJ = calendarQueue.o

all: $(OBJ)
	ar cr libcalendar.a $(OBJ)
	ranlib libcalendar.a

clean:
	rm -f libcalendar.a *.o 
