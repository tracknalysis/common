/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tracknalysis.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.tracknalysis.common.io.SocketManager;

import org.slf4j.Logger;


/**
 * @author David Smith
 * @author David Valeri
 */
public abstract class AbstractIoManager implements IoManager {
    
    private final SocketManager delegate;
    
    public AbstractIoManager(SocketManager delegate) {
        super();
        this.delegate = delegate;
    }
    
    @Override
    public synchronized final void connect() throws IOException {
        delegate.connect();
    }
    
    @Override
    public synchronized final void disconnect() throws IOException {
        delegate.disconnect();
    }

    @Override
    public synchronized IoManagerResult write(byte[] command) throws IOException {
        return write(command, null);
    }
    
    @Override
    public synchronized IoManagerResult write(byte[] command, IoProtocolHandler protocolHandler)
    		throws IOException {
    	IoManagerResult result = new IoManagerResult();
        result.requestStartTime = System.currentTimeMillis();
        
        OutputStream os = getOutputStream();
        
        byte[] bytesToSend = command;
        if (protocolHandler != null) {
        	bytesToSend = protocolHandler.wrapRequest(bytesToSend);
        }
        
        result.requestTxStartTime = System.currentTimeMillis();
        synchronized (this) {
	        os.write(bytesToSend);
	        os.flush();
        }
        result.requestTxEndTime = System.currentTimeMillis();
        
        getLogger().debug("Wrote bytes {}", command);
        
        result.requestRxStartTime = result.requestTxEndTime;
        result.requestRxEndTime = result.requestTxEndTime;
        result.requestEndTime = System.currentTimeMillis();
        
        return result;
    }

    @Override
    public IoManagerResult writeAndRead(byte[] command, long delay) throws IOException {
        return writeAndRead(command, delay, null);
    }
    
    @Override
	public IoManagerResult writeAndRead(byte[] command, long delay,
			IoProtocolHandler protocolHandler) throws IOException {
    	IoManagerResult result = new IoManagerResult();
    	
    	result.requestStartTime = System.currentTimeMillis();
    	
    	byte[] bytesToSend = command;
        if (protocolHandler != null) {
        	bytesToSend = protocolHandler.wrapRequest(bytesToSend);
        }
    	
        synchronized (this) {
	    	flushAll();
	    	
	    	IoManagerResult writeResult = write(bytesToSend, protocolHandler);
	    	result.requestTxStartTime = writeResult.requestTxStartTime;
	    	result.requestTxEndTime = writeResult.requestTxEndTime;
	    	
	    	delay(delay);
	    	
	    	IoManagerResult readResult = read(protocolHandler);
	    	result.result = readResult.result;
	    	result.requestRxStartTime = readResult.requestRxStartTime;
	    	result.requestRxEndTime = readResult.requestRxEndTime;
	    	result.requestEndTime = System.currentTimeMillis();
        }
    	
        return result;
    }
    
    @Override
	public IoManagerResult writeAndRead(byte[] command,
			byte[] result, long timeout) throws IOException {
        return writeAndRead(command, result, timeout, null);
    }
    
    @Override
    public IoManagerResult writeAndRead(byte[] command, byte[] out,
    		long timeout, IoProtocolHandler protocolHandler) throws IOException {
    	IoManagerResult result = new IoManagerResult();
    	result.requestStartTime = System.currentTimeMillis();
    	
    	byte[] bytesToSend = command;
        if (protocolHandler != null) {
        	bytesToSend = protocolHandler.wrapRequest(bytesToSend);
        }
    	
        synchronized (this) {
	    	flushAll();
	    	
	    	IoManagerResult writeResult = write(command, protocolHandler);
	    	result.requestTxStartTime = writeResult.requestTxStartTime;
	    	result.requestTxEndTime = writeResult.requestTxEndTime;
	    	
	    	IoManagerResult readResult = read(out, timeout);
	    	result.result = readResult.result;
	    	result.requestRxStartTime = readResult.requestRxStartTime;
	    	result.requestRxEndTime = readResult.requestRxEndTime;
	    	result.requestEndTime = System.currentTimeMillis();
        }
        
        return result;
    }
    
    @Override
    public IoManagerResult read() throws IOException {
    	return read(null);
    }
    
    @Override
    public IoManagerResult read(IoProtocolHandler protocolHandler)
    		throws IOException {
    	IoManagerResult result = new IoManagerResult();
    	result.requestStartTime = System.currentTimeMillis();
    	result.requestTxStartTime = result.requestStartTime;
    	result.requestTxEndTime = result.requestStartTime;
    	
        InputStream is = getInputStream();
        List<Byte> read = new ArrayList<Byte>();
        
        result.requestRxStartTime = System.currentTimeMillis();
        
        synchronized (this) {
	        while (is.available() > 0) {
	            byte b = (byte) is.read();
	            read.add(b);
	        }
        }
    
        byte[] out = new byte[read.size()];
        int i = 0;
        for (Byte b : read) {
        	out[i++] = b;
        }
        
        result.requestRxEndTime = System.currentTimeMillis();
        
        if (protocolHandler != null) {
        	result.result = protocolHandler.unwrapResponse(out);
        } else {
        	result.result = out;
        }
        
        getLogger().debug("Read bytes {}", out);
        
        result.requestEndTime = System.currentTimeMillis();
        
        return result;
    }
    
    @Override
    public IoManagerResult read(byte[] result, long timeout) throws IOException {
    	return read(result, timeout, null);
    }
    
    @Override
    public synchronized void flushAll() throws IOException {
        getOutputStream().flush();
        
        InputStream is = getInputStream();
        Logger log = getLogger();
        
        if (is.available() > 0) {
            StringBuilder b = null;
            int bytesRead = 0;       
            if (getLogger().isDebugEnabled()) {
                b = new StringBuilder();
            }
            while(is.available() > 0) {
                if (log.isDebugEnabled()) {
                    b.append(String.format("%02x ", is.read()));
                    bytesRead++;
                } else {
                    is.read();
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Discarded {} bytes: {}", bytesRead, b);
            }
        }
        
        log.debug("Flushed streams.");
    }
    
    protected void delay(long delayPeriod) {
        try {
            Thread.sleep(delayPeriod);
        } catch (InterruptedException e) {
            getLogger().warn("Interrupted during delay.", e);
        }
    }
    
    protected final SocketManager getDelegate() {
        return this.delegate;
    }
    
    protected OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }
    
    protected InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }
    
    protected abstract Logger getLogger();
}
