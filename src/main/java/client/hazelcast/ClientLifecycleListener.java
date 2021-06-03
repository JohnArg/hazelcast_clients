package client.hazelcast;

import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;

import static com.hazelcast.core.LifecycleEvent.LifecycleState.CLIENT_DISCONNECTED;

public class ClientLifecycleListener implements LifecycleListener {

    private boolean disconnected;

    public ClientLifecycleListener(){
        disconnected = false;
    }

    @Override
    public void stateChanged(LifecycleEvent lifecycleEvent) {
        if(lifecycleEvent.getState().equals(CLIENT_DISCONNECTED)){
            disconnected = true;
        }
    }

    public boolean isDisconnected() {
        return disconnected;
    }
}
