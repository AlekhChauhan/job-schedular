package schedular;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
public class Main {
    public static void main(String[] args) throws InterruptedException {
        JobSchedular schedular = new JobSchedular(4);
        schedular.start();
        long now = System.currentTimeMillis();
        schedular.schedule(new Job("job-immediate", now,0,1,()->System.out.println("Running IMMEDIATE at"+System.currentTimeMillis())));
        schedular.schedule((new Job("job-delayed", now+2000,0,1,()->System.out.println("Running DELAYED at"+ System.currentTimeMillis()))));
        Thread.sleep(3000);
        int jobCount=100;
        AtomicInteger completedCount=new AtomicInteger(0);
        CountDownLatch latch=new CountDownLatch(jobCount);
        for(int i=0;i<jobCount;i++){
            int jobNum=i;
            long randomDelay= ThreadLocalRandom.current().nextLong(0,5000);
            schedular.schedule(new Job("load-job-"+ jobNum,System.currentTimeMillis()+ randomDelay,0,1,()->{
                completedCount.incrementAndGet();
                latch.countDown();
            }));
        }
        boolean allCompleted= latch.await(10,java.util.concurrent.TimeUnit.SECONDS);
        System.out.println("Load test:" + completedCount.get()+"/"+jobCount+" jobs completed. All done:"+ allCompleted);
        schedular.shutdown();
        System.out.println("Shut Down.");
    }
}
