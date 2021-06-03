package client.hazelcast;

import java.io.Serializable;

/**
 * Used to create a payload for an IAtomicReference Hazelcast request.
 */
public class AtomicRefLoad implements Serializable {
    public static final long serialVersionId = 1L;

    public byte[] payload;

    public AtomicRefLoad(byte[] payload){
        this.payload = payload;
    }

}
