package client.hazelcast.timed;

import client.hazelcast.MillisTimer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.spi.exception.TargetDisconnectedException;

/**
 * Fires Atomic Long requests
 */
public class HzAtomicLongIncrement {

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
        // keep firing requests until timeout
        MillisTimer millisTimer = new MillisTimer();
        millisTimer.start();
        long expectedLong = 0;
        int increments = 0;
        long actualLong = 0;
        atomicLong.set(expectedLong);
        System.out.println("Starting with value : " + expectedLong);
        do{
            expectedLong ++;
            try {
                actualLong = atomicLong.incrementAndGet();
                increments ++;
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
        System.out.println("Finished with value : " + actualLong);
        System.out.println("Incremented counter "+increments+" times");

        hzClient.shutdown();

    }
}
