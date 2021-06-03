package client.hazelcast.timed;

import client.hazelcast.AtomicRefLoad;
import client.hazelcast.MillisTimer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.spi.exception.TargetDisconnectedException;

/**
 * Fires Atomic Long requests
 */
public class HzAtomicReferenceRequests {

    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Provide a timeout (in seconds) and a buffer size");
            System.exit(1);
        }
        int timeout = Integer.parseInt(args[0]);
        int bufferSize = Integer.parseInt(args[1]);

        // start client - auto-connect to cluster
        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();
        // Use CP subsystem
        CPSubsystem cpSubsystem = hzClient.getCPSubsystem();
        // Fire requests ==============================================
        // Get an atomic reference
        IAtomicReference<AtomicRefLoad> remotePayload = cpSubsystem.getAtomicReference("payload");
        // create an initial payload
        byte[] initPayload = new byte[bufferSize];
        for(int i=0; i < bufferSize; i++){
            initPayload[i] = 0b1;
        }
        AtomicRefLoad expectedLoad = new AtomicRefLoad(initPayload);

        // keep firing requests until timeout
        MillisTimer millisTimer = new MillisTimer();
        millisTimer.start();
        boolean change = true;
        do{
            AtomicRefLoad actualLoad = null;
            try {
                remotePayload.set(expectedLoad);
                actualLoad = remotePayload.get();
            }catch (TargetDisconnectedException e){
                System.out.println("[SHUTDOWN] Client disconnected");
                break;
            }
            // check if the payload has been uploaded correctly
            for(int i=0; i < expectedLoad.payload.length; i++){
                if(expectedLoad.payload[i] != actualLoad.payload[i]){
                    System.err.println("[ERROR] Payloads don't match.");
                    System.exit(1);
                }
            }
            // change payload
            if(change){
                expectedLoad.payload[0] = 0b0;
                expectedLoad.payload[expectedLoad.payload.length - 1] = 0b0;
                change = false;
            }else {
                expectedLoad.payload[0] = 0b1;
                expectedLoad.payload[expectedLoad.payload.length - 1] = 0b1;
                change = true;
            }

            millisTimer.stop();
        }while (millisTimer.getElapsedSeconds() < timeout);

        hzClient.shutdown();

    }
}
