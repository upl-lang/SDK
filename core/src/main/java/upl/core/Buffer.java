  package upl.core;
  /*
   Created by Acuna on 15.02.2019
  */
  
  import java.nio.ByteBuffer;
  import java.nio.ByteOrder;
  import java.nio.FloatBuffer;
  import java.nio.ShortBuffer;
  
  public class Buffer {
    
    public static FloatBuffer toFloatBuffer (float[] array) {
      
      ByteBuffer bBuffer = ByteBuffer.allocateDirect (Int.size (array) * 4);
      
      bBuffer.order (ByteOrder.nativeOrder ());
      
      FloatBuffer fBuffer = bBuffer.asFloatBuffer ();
      
      fBuffer.put (array);
      fBuffer.position (0);
      
      return fBuffer;
      
    }
    
    public static ShortBuffer toShortBuffer (short[] v) {
      
      ByteBuffer buf = ByteBuffer.allocateDirect (v.length * 2);
      
      buf.order (ByteOrder.nativeOrder ());
      
      ShortBuffer buffer = buf.asShortBuffer ();
      
      buffer.put (v);
      buffer.position (0);
      
      return buffer;
      
    }
    
  }