package client.hazelcast.multithreaded.async.threads;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;

/**
 * Manages the threads that will act as clients to the Hazelcast
 * cluster and will fire requests to the cluster during a
 * benchmark. It divides the number of benchmark iterations
 * approximately equally to all client threads. Each such thread
 * creates its own client to the Hazelcast cluster and sends
 * as many requests to the cluster as the number of benchmark
 * iterations it was assigned.
 */
public abstract class BenchmarkRunner {
    protected int numThreads;
    protected BenchmarkThread[] benchmarkThreads;
    protected HazelcastInstance hzClient;
    protected CPSubsystem cpSubsystem;


    public BenchmarkRunner(int numThreads) {
        this.numThreads = numThreads;
        // prepare Hazelcast client that will initialize the CP data structure for
        // the benchmark
        this.hzClient = HazelcastClient.newHazelcastClient();
        this.cpSubsystem = hzClient.getCPSubsystem();
    }

    /**
     * Assign a number of iterations to each client thread.
     * @param benchmarkIterations the total number of benchmark iterations.
     */
    public void assignIterationsToThreads(int benchmarkIterations){
        int dividedIterations = benchmarkIterations / numThreads;
        int remainderIterations = benchmarkIterations % numThreads;
        for(int i=0; i<numThreads; i++){
           benchmarkThreads[i].setThreadIterations(dividedIterations);
        }
        for(int i=0; (i<numThreads) && (remainderIterations > 0); i++, remainderIterations--){
            benchmarkThreads[i].addThreadIterations(1);
        }
        // verify
        for(int i=0; i<numThreads; i++){
            System.out.println("Client thread "+ i + " gets " + benchmarkThreads[i].getThreadIterations()
                    + " benchmark iterations.");
        }
    }

    /**
     * Perform preparations for the benchmark
     */
    public abstract void prepareBenchmark(int benchmarkIterations);

    /**
     * Start running the multi-threaded benchmark code.
     */
    public abstract void runBenchmark();

    /**
     * Run after the time measurements of the benchmark are
     * taken, to shut down the internal Hazelcast clients of
     * the benchmark threads.
     */
    public void shutDown(){
        for(int i=0; i<numThreads; i++){
            benchmarkThreads[i].shutDown();
        }
        hzClient.shutdown();
    }
}
