package schedular;
import java.util.PriorityQueue;
public class HeapTest {
    public static void main(String[] args){
        PriorityQueue<Job>heap=new PriorityQueue<>();
        long now=System.currentTimeMillis();
        heap.add(new Job("job-A",now+3000,0,1,()->{}));
        heap.add(new Job("job-B",now+1000,0,1,()->{}));
        heap.add(new Job("job-C",now+2000,0,1,()->{}));
        while(!heap.isEmpty()){
            System.out.println(heap.poll());
        }
    }
}
