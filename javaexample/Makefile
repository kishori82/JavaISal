
#IFLAGS=-I/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/include/ -I../include  -I/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/include/darwin 

LFLAGS=-L.
LIBFLAGS=-lisal

all:  ErasureCode.class  ErasureCode.h 

all += liberasure.o



ErasureCode.class:  ErasureCode.java
	javac ErasureCode.java

ErasureCode.h:  ErasureCode.class
	javah -jni ErasureCode

liberasure.so:  ErasureCode.h
	gcc -fPIC $(IFLAGS)  -c ErasureCode.c -o ErasureCode.o $(LFLAGS)
	gcc -shared  $(IFLAGS) $(LFLAGS)  -o liberasure.o ErasureCode.o  $(LIBFLAGS)

test:
	java  -Djava.library.path=. ErasureCode
clean:
	rm -rf ErasureCode.class ErasureCode.h liberasure.o
