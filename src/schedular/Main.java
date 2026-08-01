package schedular;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
public class Main {
    public static void main(String[] args) throws InterruptedException {
        JobScheduler schedular = new JobScheduler(4);
        schedular.start();
        long now = System.currentTimeMillis();
        schedular.schedule(new Job("job-immediate", now,0,1,()->System.out.println("Running IMMEDIATE at" + System.currentTimeMillis())));
        schedular.schedule((new Job("job-delayed", now+2000,0,1,()->System.out.println("Running DELAYED at" + System.currentTimeMillis()))));
        schedular.schedule(new Job("job-repeating", now+500,1000,1,()->System.out.println("Running REPEATING at" + System.currentTimeMillis())));
        AtomicInteger failCount=new AtomicInteger(0);
        schedular.schedule((new Job("job-flaky", now+100,0,1,()->{
            if(failCount.getAndIncrement()<2){
                throw new RuntimeException("simulated failure #" + failCount.get());
            }
            System.out.println("job-flaky finally SUCCEEDED at" + System.currentTimeMillis());
        })));
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
        System.out.println("Load test:" + completedCount.get()+"/"+jobCount+" jobs completed. All done:" + allCompleted);
        schedular.shutdown();
        System.out.println("Shut Down.");
    }
}
