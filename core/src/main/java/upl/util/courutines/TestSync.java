  package upl.util.courutines;
  
  public class TestSync extends Test {
    
    public static class Fib extends CorRunSync<Integer, Integer> {
      
      @Override
      public Integer call () {
        
        Integer times = getReceiveValue ();
        
        int a = 1, b = 1;
        
        for (int i = 0; times != null && i < times; i++) {
          
          int temp = a + b;
          a = b;
          b = temp;
          
        }
        
        this.yield (a);
        
        return getResult ();
        
      }
      
    }
    
    @Override
    public String call () {
      
      CorRun<Integer, Integer> fib = new Fib ();
      
      StringBuilder result = new StringBuilder ();
      Integer current;
      int times = 10;
      
      for (int i = 0; i < times; i++) {
        
        try {
          
          current = yieldFrom (fib, i);
          
          if (fib.getError () != null)
            throw new RuntimeException (fib.getError ());
          
          if (current == null)
            continue;
          
          if (i > 0) result.append (",");
          
          result.append (current);
          
        } catch (InterruptedException e) {
          return null;
        }
        
      }
      
      stop ();
      setResult (result.toString ());
      
      if (result.toString ().equals (""))
        throw new RuntimeException ("Error");
      
      return result.toString ();
      
    }
    
  }