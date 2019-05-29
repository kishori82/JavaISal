import java.util.*;

class ErasureCode {
     private native void create_encode_decode_matrix(int k, int p);
     private native void destroy_encode_decode_matrix();
     private native void cmain(byte[] data);
     private native byte[][] encode_data(byte[] data);
     private native byte[][] decode_data(byte[][] encoded_data, int []erased_indices);

     public static void main(String[] args) {

         /* this is the code parameters [m, k], m= k + p */
         int k = 10, p =4;
         /* set the parameters for once*/
         new ErasureCode().create_encode_decode_matrix(k, p);
         /* random class to generate random data */
         Random rand = new Random();
         /* number of tests, generating data, encoding, then erasing and then decoding */ 
         int numTests = 1000;
         for(int i =0; i < numTests; i++) {
           /* STEP 1 create the random data for the test */
           int datasize = 10011;
           byte[] nbyte = new byte[datasize];
           rand.nextBytes(nbyte);

           /* STEP 2 Encode the data */
           System.out.printf("Now the output is redirected! %d\n", i); 
           byte[][] encoded_data = new ErasureCode().encode_data(nbyte);

           /* print the stats of the encoded data */
           System.out.printf("Number of coded element arrays %d\n", encoded_data.length);
           int l=0;
           for(int j =0; j < encoded_data.length; j++) {
               System.out.printf("\tLength an encoded array %d\n", encoded_data[j].length);
               boolean match = true;
               for(int m=0; m < encoded_data[j].length && l < datasize; m++) {
                   if(encoded_data[j][m]!=nbyte[l]) match=false;
                   l++;
               }
               if(match==false) 
                  System.out.printf("\tdata and encoded array %d does NOT match\n", j);
               else
                  System.out.printf("\tdata and encoded array %d does matches!\n", j);
           }
 
           /* STEP 3 store the encoded data for comparison in STEP 6 */
           byte[][] orig_encoded_data = new byte[k][encoded_data[0].length];
           for (int j = 0; j < k; j++) 
                for(int m=0; m < encoded_data[j].length; m++) 
                   orig_encoded_data[j][m] = encoded_data[j][m];


           /* STEP 4 add the erasures */
           int[] erased_indices = new int[p];
           for (int j = 0; j < p; j++) {
                erased_indices[j] =  rand.nextInt(k + p);
                System.out.printf("\tErased_index %d\n", erased_indices[j]);
                /* erasing all to 0 */
                for(int m=0; m < encoded_data[erased_indices[j]].length; m++) 
                   encoded_data[erased_indices[j]][m]='0';
           }

           /* STEP 5  now decode the data */
           byte[][] decoded_data = new ErasureCode().decode_data(encoded_data, erased_indices);

           /* STEP 6 check the correctness of the data */
           System.out.printf("Number of decoded elements %d\n", decoded_data.length);
           for(int j =0; j < k; j++) {
                boolean blnResult = Arrays.equals(orig_encoded_data[j], decoded_data[j]);
                System.out.printf("Are byte (%d) arrays equal ? : %b\n",  j,blnResult);
                if( blnResult==false){ System.out.printf("Num tests %d\n", i);  System.exit(0); }
           }
         }

         new ErasureCode().destroy_encode_decode_matrix();
         System.out.printf("Passed all %d tests\n", numTests); 
     }
     static {
         System.loadLibrary("erasure");
     }
 }
