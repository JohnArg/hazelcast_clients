package client.hazelcast.timed;

import client.hazelcast.ClientLifecycleListener;
import client.hazelcast.MillisTimer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.ISemaphore;

/**
 * Fires Atomic Long requests
 */
public class HzSemaphoreRequests {

    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Provide a timeout (in seconds)");
            System.exit(1);
        }
        int timeout = Integer.parseInt(args[0]);

        // start client - auto-connect to cluster
        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();
        ClientLifecycleListener clientLifecycleListener = new ClientLifecycleListener();
        hzClient.getLifecycleService().addLifecycleListener(clientLifecycleListener);
        // Use CP subsystem
        CPSubsystem cpSubsystem = hzClient.getCPSubsystem();
        // Fire requests ==============================================
        // Get a semaphore
        ISemaphore semaphore = cpSubsystem.getSemaphore("sema");
        semaphore.init(1);
        // keep firing requests until timeout
        MillisTimer millisTimer = new MillisTimer();
        millisTimer.start();
        do{
            try {
                semaphore.acquire();
                semaphore.release();
            }catch (Exception e){
                if(!clientLifecycleListener.isDisconnected()) {
                    System.err.println("[ERROR] Error during semaphore operation");
                }
                break;
            }
            millisTimer.stop();
        }while (millisTimer.getElapsedSeconds() < timeout);

        hzClient.shutdown();
    }
}
