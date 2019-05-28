#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include "erasure_code.h"	// use <isa-l.h> instead when linking against installed
#include <jni.h>
#include <stdio.h>
#include "ErasureCode.h"
 

#define MMAX 255
#define KMAX 255

typedef unsigned char u8;
u8 *frag_ptrs[MMAX];
u8 *recover_srcs[KMAX];
u8 *recover_outp[KMAX];
u8 frag_err_list[MMAX];
u8 decode_index[MMAX];



int usage(void)
{
	fprintf(stderr,
		"Usage: ec_simple_example [options]\n"
		"  -h        Help\n"
		"  -k <val>  Number of source fragments\n"
		"  -p <val>  Number of parity fragments\n"
		"  -l <val>  Length of fragments\n"
		"  -e <val>  Simulate erasure on frag index val. Zero based. Can be repeated.\n"
		"  -r <seed> Pick random (k, p) with seed\n");
	exit(0);
}

static int gf_gen_decode_matrix_simple(u8 * encode_matrix,
				       u8 * decode_matrix,
				       u8 * invert_matrix,
				       u8 * temp_matrix,
				       u8 * decode_index,
				       u8 * frag_err_list, int nerrs, int k, int m);

 
 JNIEXPORT void JNICALL 
 Java_ErasureCode_print(JNIEnv *env, jobject obj)
 {
     static int i = 0;
     printf("Hello World! %d\n", i);
     i++;
     return;
 }

u8 *encode_matrix =NULL, *decode_matrix=NULL;
u8 *invert_matrix=NULL, *temp_matrix=NULL;
u8 *g_tbls=NULL;
int k = 10;
int p = 4; 
int len = 8 * 1024;	// Default params

JNIEXPORT void JNICALL Java_ErasureCode_create_1encode_1decode_1matrix
  (JNIEnv *env, jobject obj, jint _k, jint _p)
{
        k = _k; p = _p;
        int m = k + p;
	// Allocate coding matrices
	encode_matrix = malloc(m * k);
	decode_matrix = malloc(m * k);
	invert_matrix = malloc(m * k);
	temp_matrix = malloc(m * k);
	g_tbls = malloc(k * p * 32);

	if (encode_matrix == NULL || decode_matrix == NULL
	    || invert_matrix == NULL || temp_matrix == NULL || g_tbls == NULL) {
		printf("Test failure! Error with malloc\n");
		return ;
	}
	gf_gen_cauchy1_matrix(encode_matrix, m, k);

	ec_init_tables(k, p, &encode_matrix[k * k], g_tbls);
	// Allocate the src & parity buffers
        int i;
	for (i = 0; i < m; i++) {
		if (NULL == (frag_ptrs[i] = malloc(len))) {
			printf("alloc error: Fail\n");
			return ;
		}
	}

	// Allocate buffers for recovered data
	for (i = 0; i < p; i++) {
		if (NULL == (recover_outp[i] = malloc(len))) {
			printf("alloc error: Fail\n");
			return ;
		}
	}


}

JNIEXPORT void JNICALL Java_ErasureCode_destroy_1encode_1decode_1matrix
  (JNIEnv *env, jobject obj)
{
	// Allocate coding matrices
	if( encode_matrix == NULL) free(encode_matrix);
	if(decode_matrix == NULL) free(decode_matrix);
	if(invert_matrix == NULL) free(invert_matrix);
	if(temp_matrix == NULL) free(temp_matrix);
	if(g_tbls == NULL) free(g_tbls);

	encode_matrix = NULL;
        decode_matrix = NULL;
	invert_matrix = NULL;
        temp_matrix = NULL;
        g_tbls = NULL;
}

JNIEXPORT void JNICALL 
Java_ErasureCode_cmain(JNIEnv *env, jobject obj, jbyteArray indata)
{
       unsigned char* buffer = (*env)->GetByteArrayElements(env, indata, NULL);
       jsize size = (*env)->GetArrayLength(env, indata);
        

	int i, j, m, c, e, ret;
	int nerrs = 2;

	m = k + p;
	printf("ec_simple_example data size: %d\n", size);

	g_tbls = malloc(k * p * 32);

	// Fill sources with random data
	for (i = 0; i < k; i++)
		for (j = 0; j < len; j++)
			frag_ptrs[i][j] = rand();

	printf(" encode (m,k,p)=(%d,%d,%d) len=%d\n", m, k, p, len);

	// Pick an encode matrix. A Cauchy matrix is a good choice as even
	// large k are always invertable keeping the recovery rule simple.
	//gf_gen_cauchy1_matrix(encode_matrix, m, k);

	// Initialize g_tbls from encode matrix
	//ec_init_tables(k, p, &encode_matrix[k * k], g_tbls);

	// Generate EC parity blocks from sources
	ec_encode_data(len, k, p, g_tbls, frag_ptrs, &frag_ptrs[k]);

	if (nerrs <= 0)
		return;

	printf(" recover %d fragments\n", nerrs);

	// Find a decode matrix to regenerate all erasures from remaining frags
	ret = gf_gen_decode_matrix_simple(encode_matrix, decode_matrix,
					  invert_matrix, temp_matrix, decode_index,
					  frag_err_list, nerrs, k, m);
	if (ret != 0) {
		printf("Fail on generate decode matrix\n");
		return ;
	}
	// Pack recovery array pointers as list of valid fragments
	for (i = 0; i < k; i++)
		recover_srcs[i] = frag_ptrs[decode_index[i]];

	// Recover data
	ec_init_tables(k, nerrs, decode_matrix, g_tbls);
	ec_encode_data(len, k, nerrs, g_tbls, recover_srcs, recover_outp);

	// Check that recovered buffers are the same as original
	printf(" check recovery of block {");
	for (i = 0; i < nerrs; i++) {
		printf(" %d", frag_err_list[i]);
		if (memcmp(recover_outp[i], frag_ptrs[frag_err_list[i]], len)) {
			printf(" Fail erasure recovery %d, frag %d\n", i, frag_err_list[i]);
			return ;
		}
	}

	printf(" } done all: Pass\n");
}

/*
 * Generate decode matrix from encode matrix and erasure list
 *
 */

static int gf_gen_decode_matrix_simple(u8 * encode_matrix,
				       u8 * decode_matrix,
				       u8 * invert_matrix,
				       u8 * temp_matrix,
				       u8 * decode_index, u8 * frag_err_list, int nerrs, int k,
				       int m)
{
	int i, j, p, r;
	int nsrcerrs = 0;
	u8 s, *b = temp_matrix;
	u8 frag_in_err[MMAX];

	memset(frag_in_err, 0, sizeof(frag_in_err));

	// Order the fragments in erasure for easier sorting
	for (i = 0; i < nerrs; i++) {
		if (frag_err_list[i] < k)
			nsrcerrs++;
		frag_in_err[frag_err_list[i]] = 1;
	}

	// Construct b (matrix that encoded remaining frags) by removing erased rows
	for (i = 0, r = 0; i < k; i++, r++) {
		while (frag_in_err[r])
			r++;
		for (j = 0; j < k; j++)
			b[k * i + j] = encode_matrix[k * r + j];
		decode_index[i] = r;
	}

	// Invert matrix to get recovery matrix
	if (gf_invert_matrix(b, invert_matrix, k) < 0)
		return -1;

	// Get decode matrix with only wanted recovery rows
	for (i = 0; i < nerrs; i++) {
		if (frag_err_list[i] < k)	// A src err
			for (j = 0; j < k; j++)
				decode_matrix[k * i + j] =
				    invert_matrix[k * frag_err_list[i] + j];
	}

	// For non-src (parity) erasures need to multiply encode matrix * invert
	for (p = 0; p < nerrs; p++) {
		if (frag_err_list[p] >= k) {	// A parity err
			for (i = 0; i < k; i++) {
				s = 0;
				for (j = 0; j < k; j++)
					s ^= gf_mul(invert_matrix[j * k + i],
						    encode_matrix[k * frag_err_list[p] + j]);
				decode_matrix[k * p + i] = s;
			}
		}
	}
	return 0;
}
