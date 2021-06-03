package client.hazelcast.multithreaded.async.threads.impl;

import client.hazelcast.NanoTimer;
import client.hazelcast.multithreaded.async.threads.BenchmarkRunner;
import client.hazelcast.multithreaded.async.threads.BenchmarkThread;
import com.hazelcast.core.Pipelining;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.IAtomicLong;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class AtomicLongIncrementBenchmarkRunner extends BenchmarkRunner {

    private static final String ATOMIC_LONG_NAME = "atLong";
    protected int benchmarkIterations;
    private IAtomicLong atomicLong;
    private CyclicBarrier barrier;

    public AtomicLongIncrementBenchmarkRunner(int numThreads) {
        super(numThreads);
        benchmarkThreads = new AtomicLongIncrementThread[numThreads];
        this.barrier = new CyclicBarrier(numThreads + 1);
        // Prepare CP data structure
        this.atomicLong = cpSubsystem.getAtomicLong(ATOMIC_LONG_NAME);
    }

    @Override
    public void prepareBenchmark(int benchmarkIterations) {
        this.benchmarkIterations = benchmarkIterations;
        atomicLong.set(1);
        barrier.reset();
        for(int i=0; i<numThreads; i++){
            benchmarkThreads[i] = new AtomicLongIncrementThread(this.barrier);
        }
        assignIterationsToThreads(benchmarkIterations);
    }

    @Override
    public void runBenchmark(){
        NanoTimer timer = new NanoTimer();
        // Run benchmark ----------------------
        for(int i=0; i<numThreads; i++){
            benchmarkThreads[i].start();
        }
        try {
            // wait for all threads to get ready and then start
            barrier.await();
            timer.start();
            for (int i = 0; i < numThreads; i++) {
                benchmarkThreads[i].join();
            }
            timer.stop();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        // End benchmark. Measure Time/Throughput ----------------------
        double elapsedSeconds = timer.getElapsedSeconds();
        double throughput = (double) benchmarkIterations/elapsedSeconds;
        System.out.printf("[BENCHMARK] Performed %d operations within %.4f seconds. Throughput : %.4f ops." +
                        " Last value : "+ atomicLong.get() +".\n",
                benchmarkIterations, elapsedSeconds, throughput);
    }

    private class AtomicLongIncrementThread extends BenchmarkThread {
        private IAtomicLong atomicLong;
        private CyclicBarrier barrier;

        public AtomicLongIncrementThread(CyclicBarrier barrier) {
            super();
            this.barrier = barrier;
            CPSubsystem cpSubsystem = hzClient.getCPSubsystem();
            this.atomicLong = cpSubsystem.getAtomicLong(ATOMIC_LONG_NAME);
        }

        @Override
        public void run(){
            try {
                int depth = 30000;
                int depthIterations = threadIterations / depth;
                int remainderIterations = threadIterations % depth;
                Pipelining<Long> longValues = new Pipelining<>(depth);

                barrier.await();

                for(int i=0; i<threadIterations; i++){
                    int asyncRequests = (depthIterations > 0) ? depth : remainderIterations;
                    for(int j=0; j<asyncRequests; j++) {
                        longValues.add(atomicLong.incrementAndGetAsync());
                    }
                    depthIterations --;
                    longValues.results();
//                    List<Long> longResults = longValues.results();
//                    if(longResults.size() < depth){
//                        System.err.println("[Error] Got Fewer results.");
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
