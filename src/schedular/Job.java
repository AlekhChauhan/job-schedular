package schedular;

public class Job implements Comparable<Job>{
    String id;
    long nextRunTime;
    long intervalMillis;
    int priority;
    Runnable task;

    public Job(String id, long nextRunTime, long intervalMillis, int priority, Runnable task ){
        this.id=id;
        this.nextRunTime= nextRunTime;
        this.intervalMillis= intervalMillis;
        this.priority= priority;
        this.task= task;
    }

    public String getId() {
        return id;
    }

    public long getNextRunTime() {
        return nextRunTime;
    }

    public void setNextRunTime(long nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }
    public boolean isRepeating(){
        return intervalMillis>0;
    }
    public int getPriority(){
        return priority;
    }
    public Runnable getTask(){
        return task;
    }

    @Override
    public int compareTo(Job other){
        return Long.compare(this.nextRunTime,other.nextRunTime);
    }
    @Override
    public String toString(){
        return "Job{id="+ id +",nextRunTime=" + nextRunTime +",intervalMillis="+ intervalMillis +",priority="+ priority +"}";
    }
}
