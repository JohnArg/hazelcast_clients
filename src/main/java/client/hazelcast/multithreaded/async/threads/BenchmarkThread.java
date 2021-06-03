package client.hazelcast.multithreaded.async.threads;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public abstract class BenchmarkThread extends Thread{
    protected int threadIterations;
    protected HazelcastInstance hzClient;

    public BenchmarkThread() {
        this.hzClient = HazelcastClient.newHazelcastClient();
    }

    /**
     * Call after the benchmark time measurements to
     * shut down the Hazelcast client.
     */
    public void shutDown(){
        hzClient.shutdown();
    }

    public int getThreadIterations() {
        return threadIterations;
    }

    public void setThreadIterations(int threadIterations) {
        this.threadIterations = threadIterations;
    }

    public void addThreadIterations(int additionalIterations){
        this.threadIterations += additionalIterations;
    }
}
