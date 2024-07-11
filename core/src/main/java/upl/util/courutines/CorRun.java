  package upl.util.courutines;
  
  import java.util.concurrent.Callable;
  import java.util.concurrent.LinkedBlockingDeque;
  import java.util.concurrent.atomic.AtomicReference;
  
  public interface CorRun<ReceiveType, YieldReturnType> extends Runnable, Callable<YieldReturnType> {
    
    boolean start ();
    void stop ();
    void stop (Throwable throwable);
    boolean isStarted ();
    boolean isEnded ();
    Throwable getError ();
    
    ReceiveType getReceiveValue () throws InterruptedException;
    void setResult (YieldReturnType resultForOuter);
    YieldReturnType getResult ();
    
    YieldReturnType receive (ReceiveType value) throws InterruptedException;
    ReceiveType yield () throws InterruptedException;
    ReceiveType yield (YieldReturnType value) throws InterruptedException;
    <TargetReceiveType, TargetYieldReturnType> TargetYieldReturnType yieldFrom (CorRun<TargetReceiveType, TargetYieldReturnType> another) throws InterruptedException;
    <TargetReceiveType, TargetYieldReturnType> TargetYieldReturnType yieldFrom (CorRun<TargetReceiveType, TargetYieldReturnType> another, TargetReceiveType value) throws InterruptedException;
    
    class CorRunYieldReturn<ReceiveType, YieldReturnType> {
      
      public final AtomicReference<ReceiveType> receiveValue;
      public final LinkedBlockingDeque<AtomicReference<YieldReturnType>> yieldReturnValue;
      
      public CorRunYieldReturn (AtomicReference<ReceiveType> receiveValue, LinkedBlockingDeque<AtomicReference<YieldReturnType>> yieldReturnValue) {
        
        this.receiveValue = receiveValue;
        this.yieldReturnValue = yieldReturnValue;
        
      }
      
    }
    
  }