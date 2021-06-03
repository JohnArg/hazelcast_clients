package client.hazelcast.iterated;

import client.hazelcast.ClientLifecycleListener;
import client.hazelcast.NanoTimer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;

/**
 * Fires Atomic Long requests
 */
public class HzFencedLockRequests {

    public static void main(String[] args) {
        if(args.length < 3){
            System.out.println("Provide the number of warmup iterations and the number of benchmark iterations " +
                    "and the number of benchmarks to run. Results of these benchmarks will be averaged.");
            System.exit(1);
        }
        int warmupIterations = Integer.parseInt(args[0]);
        int benchIterations = Integer.parseInt(args[1]);
        int benchRepetitions = Integer.parseInt(args[2]);

        // Prepare ==============================================
        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();
        CPSubsystem cpSubsystem = hzClient.getCPSubsystem();
        FencedLock lock = cpSubsystem.getLock("flock");
        NanoTimer timer = new NanoTimer();
        int completed_operations;

        // WarmUp =================================================
        timer.start();
        for(completed_operations=0; completed_operations<warmupIterations; completed_operations++) {
            lock.lock();
            lock.unlock();
        }
        timer.stop();
        double elapsedSeconds = timer.getElapsedSeconds();
        System.out.printf("[WARMUP] Performed %d operations within %.4f seconds. Throughput : %.4f ops.\n",
                completed_operations, elapsedSeconds, ((double) completed_operations/elapsedSeconds));

        // Benchmark ==============================================
        double avgThroughput = 0.0;
        for(int i=0; i<benchRepetitions; i++) {
            timer.reset();
            timer.start();
            for(completed_operations=0; completed_operations<benchIterations; completed_operations++) {
                lock.lock();
                lock.unlock();
            }
            timer.stop();
            // Results ================================================
            elapsedSeconds = timer.getElapsedSeconds();
            double throughput = ((double) completed_operations/elapsedSeconds);
            avgThroughput += throughput;
            System.out.printf("[BENCHMARK][REP %d] Performed %d operations within %.4f seconds. Throughput : %.4f ops.\n",
                    i, completed_operations, elapsedSeconds, throughput);
        }
        avgThroughput = avgThroughput/benchRepetitions;
        System.out.printf("[BENCHMARK][AVG] Average Throughput : %.4f ops.\n", avgThroughput);

        hzClient.shutdown();
    }
}
