package client.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.*;
import com.hazelcast.cp.lock.FencedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HzCpClientTest {
    private static final Logger logger = LoggerFactory.getLogger(HzCpClientTest.class.getSimpleName());

    public static void main(String[] args) {

        // start client - auto-connect to cluster
        HazelcastInstance hzInstance = HazelcastClient.newHazelcastClient();
        // Use CP subsystem
        CPSubsystem cpSubsystem = hzInstance.getCPSubsystem();

        // Atomic Long ========================================================
        IAtomicLong atomicLong = cpSubsystem.getAtomicLong("atLong");
        long expectedLong = 123;
        atomicLong.set(expectedLong);

        for(int i=0; i < 4; i++){
            long actual = atomicLong.incrementAndGet();
            expectedLong++;
            if(expectedLong != actual){
                logger.error("Long Comparison Failed. Expected : " + expectedLong +
                        " Got : " + actual);
            }
        }
        // Atomic Reference ========================================================
        IAtomicReference<String> strRef = cpSubsystem.getAtomicReference("strRef");

        String expectedStr = "Hello RDMA!";
        strRef.set(expectedStr);
        String actualStr = strRef.get();
        if(!expectedStr.equals(actualStr)){
            logger.error("String Comparison Failed. Expected : " + expectedStr +
                    " Got : " + actualStr);
        }

        expectedStr = "Hello RDMA! Nice to meet you!";
//        IFunction<String, String> strFunction = new IFunction<String, String>() {
//            @Override
//            public String apply(String s) {
//                return s + " Nice to meet you!";
//            }
//        };
//        strRef.alter(strFunction);
        strRef.set(expectedStr);
        actualStr = strRef.get();
        if(!expectedStr.equals(actualStr)){
            logger.error("String Comparison Failed. Expected : " + expectedStr +
                    " Got : " + actualStr);
        }
        // Countdown Latch =======================================================
        ICountDownLatch countDownLatch = cpSubsystem.getCountDownLatch("cntLatch");
        if(countDownLatch == null){
            logger.error("Got null countdown latch");
        }
        // Lock ==================================================================
        FencedLock lock = cpSubsystem.getLock("lock");
        if(lock == null){
            logger.error("Got null lock");
        }
        // Semaphore =============================================================
        ISemaphore semaphore = cpSubsystem.getSemaphore("sem");
        if(semaphore == null){
            logger.error("Got null semaphore");
        }
        // Shutdown the client
        hzInstance.shutdown();
    }

}
