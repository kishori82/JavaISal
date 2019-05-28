import java.util.*;

class ErasureCode {
     private native void print();
     private native void create_encode_decode_matrix(int k, int p);
     private native void destroy_encode_decode_matrix();
     private native void cmain(byte[] data);
     private native byte[][] encode_data(byte[] data);
     private native byte[][] decode_data(byte[][] encoded_data, int []erased_indices);

     public static void main(String[] args) {
         int k = 10, p =4;
         new ErasureCode().print();
         new ErasureCode().create_encode_decode_matrix(k, p);

         Random rand = new Random();

         for(int i =0; i < 20; i++) {
           byte[] nbyte = new byte[10011];
           rand.nextBytes(nbyte);

           System.out.printf("Now the output is redirected! %d\n", i); 
           //new ErasureCode().cmain(nbyte);
           byte[][] encoded_data = new ErasureCode().encode_data(nbyte);

           System.out.printf("Number of coded element arrays %d\n", encoded_data.length);
           int[] erased_indices = new int[p];
           for (int j = 0; j < p; j++) {
                erased_indices[j] = rand.nextInt(k + p);
                System.out.printf("\tErased_index %d\n", erased_indices[j]);
           }




           byte[][] decoded_data = new ErasureCode().decode_data(encoded_data, erased_indices);
           System.out.printf("Number of decoded elements %d\n", decoded_data.length);
           for(int j =0; j < decoded_data.length; j++) {
               System.out.printf("\tLength an array %d\n", decoded_data[j].length);
           }


         }

//         new ErasureCode().cmain();
         new ErasureCode().destroy_encode_decode_matrix();
     }
     static {
         System.loadLibrary("erasure");
     }
 }
