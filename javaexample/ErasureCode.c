#include <jni.h>
#include <stdio.h>
#include "ErasureCode.h"
 
 JNIEXPORT void JNICALL 
 Java_ErasureCode_print(JNIEnv *env, jobject obj)
 {
     static int i = 0;
     printf("Hello World! %d\n", i);
     i++;
     return;
 }
