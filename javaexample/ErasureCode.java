import java.util.*;

class ErasureCode {
     private native void print();
     private native void create_encode_decode_matrix(int k, int p);
     private native void destroy_encode_decode_matrix();
     private native void cmain(byte[] data);

     public static void main(String[] args) {
         int k = 10, p =4;
         new ErasureCode().print();
         new ErasureCode().create_encode_decode_matrix(k, p);

         Random randomno = new Random();

         // create byte array
         byte[] nbyte = new byte[1000];
      // put the next byte in the array
         randomno.nextBytes(nbyte);

         new ErasureCode().cmain(nbyte);

//         new ErasureCode().cmain();
         new ErasureCode().destroy_encode_decode_matrix();
     }
     static {
         System.loadLibrary("erasure");
     }
 }
