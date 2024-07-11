  package upl.util.courutines;
  
  import java.lang.ref.WeakReference;
  import java.util.ArrayList;
  import java.util.List;
  import java.util.concurrent.atomic.AtomicBoolean;
  
  public abstract class CorRunSync<ReceiveType, YieldReturnType> implements CorRun<ReceiveType, YieldReturnType> {
    
    private ReceiveType receiveValue;
    public final List<WeakReference<CorRun<?, ?>>> potentialChildrenCoroutineList = new ArrayList<> ();
    
    // Outside
    
    private final AtomicBoolean isStarted = new AtomicBoolean (false);
    private final AtomicBoolean isEnded = new AtomicBoolean (false);
    private Throwable error;
    
    private YieldReturnType resultForOuter;
    
    @Override
    public boolean start () {
      
      boolean isStarted = this.isStarted.getAndSet (true);
      
      if (!isStarted && !isEnded ())
        receive (null);
      
      return isStarted;
      
    }
    
    @Override
    public void stop () {
      stop (null);
    }
    
    @Override
    public void stop (Throwable throwable) {
      
      isEnded.set (true);
      if (throwable != null) error = throwable;
      
      for (WeakReference<CorRun<?, ?>> weakReference : potentialChildrenCoroutineList) {
        
        CorRun<?, ?> child = weakReference.get ();
        
        if (child != null) child.stop ();
        
      }
      
    }
    
    @Override
    public boolean isStarted () {
      return isStarted.get ();
    }
    
    @Override
    public boolean isEnded () {
      return isEnded.get ();
    }
    
    @Override
    public Throwable getError () {
      return error;
    }
    
    @Override
    public ReceiveType getReceiveValue () {
      return receiveValue;
    }
    
    @Override
    public void setResult (YieldReturnType resultForOuter) {
      this.resultForOuter = resultForOuter;
    }
    
    @Override
    public YieldReturnType getResult () {
      return resultForOuter;
    }
    
    @Override
    public synchronized YieldReturnType receive (ReceiveType value) {
      
      receiveValue = value;
      
      run ();
      
      return getResult ();
      
    }
    
    @Override
    public ReceiveType yield () {
      return this.yield (null);
    }
    
    @Override
    public ReceiveType yield (YieldReturnType value) {
      
      resultForOuter = value;
      return receiveValue;
      
    }
    
    @Override
    public <TargetReceiveType, TargetYieldReturnType> TargetYieldReturnType yieldFrom (CorRun<TargetReceiveType, TargetYieldReturnType> another) throws InterruptedException {
      return yieldFrom (another, null);
    }
    
    @Override
    public <TargetReceiveType, TargetYieldReturnType> TargetYieldReturnType yieldFrom (CorRun<TargetReceiveType, TargetYieldReturnType> another, TargetReceiveType value) throws InterruptedException {
      
      if (another == null || another.isEnded ())
        throw new RuntimeException ("Call null or isEnded coroutine");
      
      potentialChildrenCoroutineList.add (new WeakReference<> (another));
      
      synchronized (another) {
        
        boolean isStarted = another.start ();
        boolean isJustStarting = !isStarted;
        
        if (isJustStarting && another instanceof CorRunSync)
          return another.getResult ();
        
        return another.receive (value);
        
      }
      
    }
    
    @Override
    public void run () {
      
      try {
        call ();
      } catch (Exception e) {
        
        e.printStackTrace ();
        stop (e);
        
      }
      
    }
    
  }