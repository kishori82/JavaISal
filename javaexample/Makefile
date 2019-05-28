
#IFLAGS=-I/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/include/ -I../include  -I/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/include/darwin 
IFLAGS=-I/usr/lib/jvm/java-8-openjdk-amd64/include/ -I/usr/lib/jvm/java-8-openjdk-amd64/include/linux -I../include 
LFLAGS=-L../bin
LIBFLAGS=-lisal
LDFLAGS=-Wl,-rpath=../bin

all: ErasureCode.class  ErasureCode.h  liberasure.so

ErasureCode.class:  ErasureCode.java
	javac ErasureCode.java

ErasureCode.h:  ErasureCode.class
	javah -jni ErasureCode

liberasure.so:  ErasureCode.h
	gcc -fPIC $(IFLAGS) $(LFLAGS)  -c ErasureCode.c -o ErasureCode.o $(LIBFLAGS) $(LDFLAGS)
	gcc -shared -fPIC  $(IFLAGS)  $(LFLAGS) -o liberasure.so ErasureCode.o  $(LIBFLAGS) $(LDFLAGS)

test:
	java  -Djava.library.path=. ErasureCode
clean:
	rm -rf ErasureCode.class ErasureCode.h liberasure.so
