package org.saavy.net.tftp;

import org.apache.commons.net.tftp.TFTPClient;
import org.saavy.platform.net.CommStackHandler;

/**
 *
 * @author rgsaavedra
 */
public class TFTPHandler extends CommStackHandler {
    
    private TFTPClient client = new TFTPClient();
    
    @Override
    public void close() {
        
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    public TFTPClient getClient() {
        return client;
    }

    public void setClient(TFTPClient client) {
        this.client = client;
    }

}
