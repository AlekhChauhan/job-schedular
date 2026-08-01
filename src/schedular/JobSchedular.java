package schedular;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class JobSchedular {
    private final PriorityQueue<Job>heap=new PriorityQueue<>();
    private final ExecutorService pool;
    private volatile boolean running=true;
    private Thread dispatcherThread;

    public JobSchedular(int poolSize){
        this.pool=Executors.newFixedThreadPool(poolSize);
    }
    public synchronized void schedule(Job job){
        heap.add(job);
        notifyAll();
    }
    public void start(){
        dispatcherThread=new Thread(this::DispatchLoop, "dispatcher");
        dispatcherThread.start();
    }
    private void DispatchLoop(){
        while(running){
            Job next;
            synchronized (this){
                while(heap.isEmpty()){
                    try{
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                next=heap.peek();
                long delay=next.getNextRunTime()-System.currentTimeMillis();
                if(delay>0){
                    try{
                        wait(delay);
                    } catch (InterruptedException e) {
                        return;
                    }
                    continue;
                }
                heap.poll();
            }
            runWithRetry(next);
        }
    }
    private static final int MAX_RETRIES=3;
    private void runWithRetry(Job job){
        pool.submit(()->{
            int attempt=0;
            boolean success=false;
            while(attempt<MAX_RETRIES && !success){
                try{
                    job.getTask().run();
                    success=true;
                } catch (Exception e) {
                    attempt++;
                    long backoffMillis=(long)Math.pow(2,attempt)*100L;
                    System.out.println("["+job.getId()+"] attempt"+attempt+"failed ("+ e.getMessage()+"),retrying in"+ backoffMillis+"ms");
                    try {
                        Thread.sleep(backoffMillis);
                    }catch (InterruptedException ignored){
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            if(!success){
                System.out.println("["+job.getId()+"] giving up after "+MAX_RETRIES+" attempts");
            }
        });
    }
    public void shutdown(){
        running=false;
        if(dispatcherThread!=null){
            dispatcherThread.interrupt();
        }
        pool.shutdown();
    }
}