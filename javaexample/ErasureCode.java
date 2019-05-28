class ErasureCode {
     private native void print();
     private native void cmain();
     public static void main(String[] args) {
         new ErasureCode().print();
         new ErasureCode().cmain();
     }
     static {
         System.loadLibrary("erasure");
     }
 }
