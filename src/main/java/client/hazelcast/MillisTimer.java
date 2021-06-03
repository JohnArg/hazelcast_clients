package client.hazelcast;

public class MillisTimer {
    private long start;
    private long stop;

    public void start(){
        start = System.currentTimeMillis();
    }

    public void stop(){
        stop = System.currentTimeMillis();
    }

    public long getElapsedSeconds(){
        return (stop - start)/1000;
    }

    public long getElapsedMillis(){
        return (stop - start);
    }

    public void reset(){
        start = 0;
        stop = 0;
    }
}
