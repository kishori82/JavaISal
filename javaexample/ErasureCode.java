class ErasureCode {
     private native void print();
     private native void create_encode_decode_matrix(int k, int p);
     private native void cmain();

     public static void main(String[] args) {
         int k = 10, p =4;
         new ErasureCode().print();
         new ErasureCode().create_encode_decode_matrix(k, p);
         new ErasureCode().cmain();
     }
     static {
         System.loadLibrary("erasure");
     }
 }
