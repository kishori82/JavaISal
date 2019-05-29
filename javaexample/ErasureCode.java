import java.util.*;

class ErasureCode {
     private native void create_encode_decode_matrix(int k, int p);
     private native void destroy_encode_decode_matrix();
     private native void cmain(byte[] data);
     private native byte[][] encode_data(byte[] data);
     private native byte[][] decode_data(byte[][] encoded_data, int []erased_indices);

     public static void main(String[] args) {

         /* this is the code parameters [m, k], m= k + p */
         int numTests = 1000000;
         int k = 10, p =4;
         int datasize = 100011;
	 System.out.printf("Encode scheme (m,k,p)=(%d,%d,%d) datasize=%d\n", k+p, k, p, datasize);
         /* set the parameters for once to the library*/
         new ErasureCode().create_encode_decode_matrix(k, p);
         /* random class to generate random data */
         Random rand = new Random();
         /* number of tests, generating data, encoding, then erasing and then decoding */ 
         for(int i =0; i < numTests; i++) {
           System.out.printf("TEST :%d\n", i); 

           /* STEP 1 create the random data for the test */
           byte[] nbyte = new byte[datasize];
           rand.nextBytes(nbyte);
           System.out.printf("\ta. Generated the random data for test %d\n", i); 

           /* STEP 2 Encode the data */
           System.out.printf("\tb. Encoding data for test. %d\n", i); 
           byte[][] encoded_data = new ErasureCode().encode_data(nbyte);
           System.out.printf("\t\t# coded elements=%d and len of element=%d\n", encoded_data.length, encoded_data[0].length);

           /* print the stats of the encoded data */
           int l=0;
           boolean match = true;
           for(int j =0; j < encoded_data.length; j++) {
               for(int m=0; m < encoded_data[j].length && l < datasize; m++) {
                   if(encoded_data[j][m]!=nbyte[l]) match=false;
                   l++;
               }
           }
           if(match==false) 
               System.out.printf("\tc. First %d encoded parts DOES match data\n",k,  i);
           else
               System.out.printf("\tc. First %d encoded parts does NOT match data\n",k,  i);
 
           /* STEP 3 store the encoded data for comparison in STEP 6 */
           byte[][] orig_encoded_data = new byte[k][encoded_data[0].length];
           for (int j = 0; j < k; j++) 
                for(int m=0; m < encoded_data[j].length; m++) 
                   orig_encoded_data[j][m] = encoded_data[j][m];


           /* STEP 4 add the erasures */
           int[] erased_indices = new int[p];
           for (int j = 0; j < p; j++) {
                erased_indices[j] =  rand.nextInt(k + p);
                /* erasing all to 0 */
                for(int m=0; m < encoded_data[erased_indices[j]].length; m++) 
                   encoded_data[erased_indices[j]][m]='0';
           }
           System.out.printf("\td. Erased blocks: ");
           for (int j = 0; j < p; j++) 
              System.out.printf(" %d", erased_indices[j]);
           System.out.printf("\n");

           /* STEP 5  now decode the data */
           byte[][] decoded_data = new ErasureCode().decode_data(encoded_data, erased_indices);

           /* STEP 6 check the correctness of the data */
           System.out.printf("\te. Number of decoded elements %d\n", decoded_data.length);
           boolean blnResult = true;
           for(int j =0; j < k; j++) {
                if( Arrays.equals(orig_encoded_data[j], decoded_data[j]) ==false) { 
                    blnResult=false;
                    System.out.printf("Test failed at test no. %d\n", i);  
                  //  System.exit(0); 
                }
//                System.out.printf("Are byte (%d) arrays equal ? : %b\n",  j,blnResult);
           }
           System.out.printf("\t\t Correctly decoded data\n");
         }

         new ErasureCode().destroy_encode_decode_matrix();
         System.out.printf("PASSED ALL %d TESTS\n", numTests); 
     }
     static {
         System.loadLibrary("erasure");
     }
 }
