package client.hazelcast.iterated;

import client.hazelcast.AtomicRefLoad;
import client.hazelcast.NanoTimer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.IAtomicReference;

/**
 * Fires Atomic Long requests
 */
public class HzAtomicReferenceRequests {

    public static void main(String[] args) {
        if(args.length < 4){
            System.out.println("Provide the number of warmup iterations and the number of benchmark iterations " +
                    ", the number of benchmarks to run and a buffer size. " +
                    "Results of these benchmarks will be averaged.");
            System.exit(1);
        }
        int warmupIterations = Integer.parseInt(args[0]);
        int benchIterations = Integer.parseInt(args[1]);
        int benchRepetitions = Integer.parseInt(args[2]);
        int bufferSize = Integer.parseInt(args[3]);

        // Prepare ==============================================
        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();
        CPSubsystem cpSubsystem = hzClient.getCPSubsystem();
        IAtomicReference<AtomicRefLoad> remotePayload = cpSubsystem.getAtomicReference("atref");
        // create an initial payload
        byte[] initPayload = new byte[bufferSize];
        for(int i=0; i < bufferSize; i++){
            initPayload[i] = 0b1;
        }
        AtomicRefLoad expectedLoad = new AtomicRefLoad(initPayload);
        NanoTimer timer = new NanoTimer();
        int completed_operations;

        // WarmUp =================================================
        timer.start();
        for(completed_operations=0; completed_operations<warmupIterations; completed_operations++) {
            remotePayload.set(expectedLoad);
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
                remotePayload.set(expectedLoad);
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
