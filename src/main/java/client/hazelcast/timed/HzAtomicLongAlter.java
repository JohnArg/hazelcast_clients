package client.hazelcast.timed;

import client.hazelcast.MillisTimer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.spi.exception.TargetDisconnectedException;
import jarg.thesis.experiments.benchmarks.hazelcast.client.DoublingFunction;

/**
 * Fires Atomic Long requests
 */
public class HzAtomicLongAlter {

    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Provide a timeout (in seconds)");
            System.exit(1);
        }
        int timeout = Integer.parseInt(args[0]);

        // start client - auto-connect to cluster
        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();
        // Use CP subsystem
        CPSubsystem cpSubsystem = hzClient.getCPSubsystem();
        // Fire requests ==============================================
        // Get an atomic long
        IAtomicLong atomicLong = cpSubsystem.getAtomicLong("atLong");
        // Create a function that alters it
        DoublingFunction doubling = new DoublingFunction();
        // keep firing requests until timeout
        MillisTimer millisTimer = new MillisTimer();
        millisTimer.start();
        long expectedLong = 1;
        long actualLong = 1;
        atomicLong.set(expectedLong);
        System.out.println("Starting with value : " + expectedLong);
        do{
            expectedLong *= 2;
            try {
                actualLong = atomicLong.alterAndGet(doubling);
            }catch (TargetDisconnectedException e){
                System.out.println("[SHUTDOWN] Client disconnected");
                break;
            }
            if(expectedLong != actualLong){
                System.err.println("[ERROR] Expected "+expectedLong+",  got "+actualLong);
                break;
            }
            millisTimer.stop();
        }while (millisTimer.getElapsedSeconds() < timeout);

        hzClient.shutdown();

    }
}
