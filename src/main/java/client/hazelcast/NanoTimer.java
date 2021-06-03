package client.hazelcast;

public class NanoTimer {
    private long start;
    private long stop;

    public void start(){
        start = System.nanoTime();
    }

    public void stop(){
        stop = System.nanoTime();
    }

    public double getElapsedSeconds(){
        return ((double)(stop - start))/(1000000000);
    }

    public void reset(){
        start = 0;
        stop = 0;
    }
}
