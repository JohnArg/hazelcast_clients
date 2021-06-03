package client.hazelcast.timed;

import client.hazelcast.ClientLifecycleListener;
import client.hazelcast.MillisTimer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;

/**
 * Fires Atomic Long requests
 */
public class HzFencedLockRequests {

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
        // Get a lock
        FencedLock lock = cpSubsystem.getLock("lock");
        // keep firing requests until timeout
        MillisTimer millisTimer = new MillisTimer();
        millisTimer.start();
        do{
            try {
                lock.lock();
                if (!lock.isLocked()) {
                    System.err.println("[ERROR] Expected lock to be locked");
                    break;
                }
                lock.unlock();
            }catch (Exception e){
                if(!clientLifecycleListener.isDisconnected()) {
                    System.err.println("[ERROR] Error during lock operation");
                }
                break;
            }
            millisTimer.stop();
        }while (millisTimer.getElapsedSeconds() < timeout);

        hzClient.shutdown();
    }
}
