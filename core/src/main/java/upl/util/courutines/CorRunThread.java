  package upl.util.courutines;
  
  import java.lang.ref.WeakReference;
  import java.util.ArrayList;
  import java.util.List;
  import java.util.concurrent.ExecutorService;
  import java.util.concurrent.Executors;
  import java.util.concurrent.Future;
  import java.util.concurrent.LinkedBlockingDeque;
  import java.util.concurrent.ThreadFactory;
  import java.util.concurrent.atomic.AtomicBoolean;
  import java.util.concurrent.atomic.AtomicReference;
  
  import javax.annotation.NonNull;
  
  public abstract class CorRunThread<ReceiveType, YieldReturnType> implements CorRun<ReceiveType, YieldReturnType> {
    
    private ExecutorService childExecutorService = newExecutorService ();
    private ExecutorService executingOnExecutorService;
    
    private static CorRunYieldReturn<?, ?> DUMMY_COR_RUN_YIELD_RETURN = new CorRunYieldReturn<> (new AtomicReference<> (null), new LinkedBlockingDeque<> ());
    
    private CorRun<ReceiveType, YieldReturnType> self;
    public List<WeakReference<CorRun<?, ?>>> potentialChildrenCoroutineList;
    private CorRunYieldReturn<ReceiveType, YieldReturnType> lastCorRunYieldReturn;
    
    private LinkedBlockingDeque<CorRunYieldReturn<ReceiveType, YieldReturnType>> receiveQueue;
    
    // Outside
    
    private AtomicBoolean isStarted = new AtomicBoolean (false);
    private AtomicBoolean isEnded = new AtomicBoolean (false);
    private Future<YieldReturnType> future;
    private Throwable error;
    
    private AtomicReference<YieldReturnType> resultForOuter = new AtomicReference<> ();
    
    public CorRunThread () {
      
      executingOnExecutorService = childExecutorService;
      
      receiveQueue = new LinkedBlockingDeque<> ();
      potentialChildrenCoroutineList = new ArrayList<> ();
      
      self = this;
      
    }
    
    @Override
    public void run () {
      
      try {
        self.call ();
      } catch (Exception e) {
        stop (e);
      }
      
      stop ();
      
    }
    
    @Override
    public abstract YieldReturnType call ();
    
    @Override
    public boolean start () {
      return start (childExecutorService);
    }
    
    protected boolean start (ExecutorService executorService) {
      
      boolean isStarted = this.isStarted.getAndSet (true);
      
      if (!isStarted) {
        
        executingOnExecutorService = executorService;
        future = (Future<YieldReturnType>) executingOnExecutorService.submit ((Runnable) self);
        
      }
      
      return isStarted;
      
    }
    
    @Override
    public void stop () {
      stop (null);
    }
    
    @Override
    public void stop (Throwable throwable) {
      
      if (throwable != null)
        error = throwable;
      
      isEnded.set (true);
      
      returnYieldValue (null);
      // Do this for making sure the coroutine has checked isEnd() after getting a dummy value
      receiveQueue.offer ((CorRunYieldReturn<ReceiveType, YieldReturnType>) DUMMY_COR_RUN_YIELD_RETURN);
      
      for (WeakReference<CorRun<?, ?>> weakReference : potentialChildrenCoroutineList) {
        
        CorRun<?, ?> child = weakReference.get ();
        
        if (child instanceof CorRunThread)
          ((CorRunThread<?, ?>) child).tryStop (childExecutorService);
        
      }
      
      childExecutorService.shutdownNow ();
      
    }
    
    protected void tryStop (ExecutorService executorService) {
      
      if (executingOnExecutorService == executorService)
        stop ();
      
    }
    
    @Override
    public boolean isEnded () {
      return isEnded.get () || (future != null && (future.isCancelled () || future.isDone ()));
    }
    
    @Override
    public boolean isStarted () {
      return isStarted.get ();
    }
    
    @Override
    public Throwable getError () {
      return error;
    }
    
    @Override
    public void setResult (YieldReturnType resultForOuter) {
      this.resultForOuter.set (resultForOuter);
    }
    
    @Override
    public YieldReturnType getResult () {
      return this.resultForOuter.get ();
    }
    
    @Override
    public YieldReturnType receive (ReceiveType value) throws InterruptedException {
      
      LinkedBlockingDeque<AtomicReference<YieldReturnType>> yieldReturnValue = new LinkedBlockingDeque<> ();
      
      offerReceiveValue (value, yieldReturnValue);
      
      return yieldReturnValue.take ().get ();
      
    }
    
    @Override
    public ReceiveType yield () throws InterruptedException {
      return this.yield (null);
    }
    
    @Override
    public ReceiveType yield (YieldReturnType value) throws InterruptedException {
      
      returnYieldValue (value);
      return getReceiveValue ();
      
    }
    
    @Override
    public <TargetReceiveType, TargetYieldReturnType> TargetYieldReturnType yieldFrom (CorRun<TargetReceiveType, TargetYieldReturnType> another) throws InterruptedException {
      return yieldFrom (another, null);
    }
    
    @Override
    public <TargetReceiveType, TargetYieldReturnType> TargetYieldReturnType yieldFrom (CorRun<TargetReceiveType, TargetYieldReturnType> another, TargetReceiveType value) throws InterruptedException {
      
      if (another == null || another.isEnded ())
        throw new RuntimeException ("Call null or isEnded coroutine");
      
      boolean isStarted;
      potentialChildrenCoroutineList.add (new WeakReference<> (another));
      
      synchronized (another) {
        
        if (another instanceof CorRunThread)
          isStarted = ((CorRunThread<?, ?>) another).start (childExecutorService);
        else
          isStarted = another.start ();
        
        boolean isJustStarting = !isStarted;
        
        if (isJustStarting && another instanceof CorRunSync)
          return another.getResult ();
        
        return another.receive (value);
        
      }
      
    }
    
    @Override
    public ReceiveType getReceiveValue () throws InterruptedException {
      
      setLastCorRunYieldReturn (takeLastCorRunYieldReturn ());
      return lastCorRunYieldReturn.receiveValue.get ();
      
    }
    
    protected void returnYieldValue (YieldReturnType value) {
      
      CorRunYieldReturn<ReceiveType, YieldReturnType> corRunYieldReturn = lastCorRunYieldReturn;
      
      if (corRunYieldReturn != null)
        corRunYieldReturn.yieldReturnValue.offer (new AtomicReference<> (value));
      
    }
    
    protected void offerReceiveValue (ReceiveType value, LinkedBlockingDeque<AtomicReference<YieldReturnType>> yieldReturnValue) {
      receiveQueue.offer (new CorRunYieldReturn<> (new AtomicReference<> (value), yieldReturnValue));
    }
    
    protected CorRunYieldReturn<ReceiveType, YieldReturnType> takeLastCorRunYieldReturn () throws InterruptedException {
      return receiveQueue.take ();
    }
    
    protected void setLastCorRunYieldReturn (CorRunYieldReturn<ReceiveType, YieldReturnType> lastCorRunYieldReturn) {
      this.lastCorRunYieldReturn = lastCorRunYieldReturn;
    }
    
    protected ExecutorService newExecutorService () {
      return Executors.newCachedThreadPool (getThreadFactory ());
    }
    
    protected ThreadFactory getThreadFactory () {
      
      return new ThreadFactory () {
        
        @Override
        public Thread newThread (@NonNull Runnable runnable) {
          
          Thread thread = new Thread (runnable);
          
          thread.setUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler () {
            
            @Override
            public void uncaughtException (Thread thread, Throwable throwable) {
              
              throwable.printStackTrace ();
              
              if (runnable instanceof CorRun<?, ?>) {
                
                CorRun<?, ?> self = (CorRun<?, ?>) runnable;
                
                self.stop (throwable);
                thread.interrupt ();
                
              }
              
            }
            
          });
          
          return thread;
        }
        
      };
      
    }
    
  }