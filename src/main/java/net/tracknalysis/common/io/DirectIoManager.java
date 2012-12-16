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

import net.tracknalysis.common.io.SocketManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation that performs input directly on the socket input and trusts
 * in {@link InputStream#available()} to behave ideally for detecting timeouts.
 * 
 * @author David Smith
 * @author David Valeri
 */
public class DirectIoManager extends AbstractIoManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(DirectIoManager.class);

    public DirectIoManager(SocketManager delegate) {
        super(delegate);
    }

    @Override
	public IoManagerResult read(byte[] out, long timeout,
			IoProtocolHandler protocolHandler) throws IOException {
    	
    	IoManagerResult result = new IoManagerResult();
    	result.requestStartTime = System.currentTimeMillis();
    	result.requestTxStartTime = result.requestStartTime;
    	result.requestTxEndTime = result.requestStartTime;
    	
    	byte[] ioOut = out;
    	if (protocolHandler != null) {
    		ioOut = new byte[protocolHandler.getWrappedResponseLength(out.length)];
    	}
    	
        InputStream is = getInputStream();
        
        long start = System.currentTimeMillis();
        long elapsedTime = 0;
        int nBytes = ioOut.length;
        int bytesRead = 0;
        int available = is.available();
        
        result.requestRxStartTime = System.currentTimeMillis();
        synchronized (this) {
	        try {
	            
	            while (bytesRead < nBytes) {
	                
	                if (LOG.isTraceEnabled()) {
	                    LOG.trace("Read {} bytes of {}.  {} bytes available.  "
	                            + "~{}ms elapsed.  ~{}ms to go before timeout",
	                            new Object[] {
	                                    bytesRead,
	                                    nBytes,
	                                    available,
	                                    elapsedTime,
	                                    timeout - elapsedTime});
	                }
	                
	                if (available > 0) {
	                    int bytesJustRead = 
	                            is.read(ioOut, bytesRead, nBytes - bytesRead);
	                    if (bytesJustRead == -1) {
	                        break;
	                    }
	                    
	                    bytesRead += bytesJustRead;
	                }
	                
	                elapsedTime = System.currentTimeMillis() - start;
	                
	                if (bytesRead != nBytes && elapsedTime > timeout) {
	                    throw new IOException("Error fulfilling read request.  Read " + bytesRead
	                            + " bytes of " + nBytes + " before exceeding timeout of "
	                            + timeout + "ms.");
	                }
	                
	                available = is.available();
	            }
	        } finally {
	            if (LOG.isDebugEnabled()) {
	                LOG.debug("Read {} bytes of {} in {}ms: ", new Object[] { bytesRead,
	                        nBytes, elapsedTime, ioOut});
	            }
	        }
        }
        result.requestRxEndTime = System.currentTimeMillis();
        
        if (bytesRead != ioOut.length) {
            throw new IOException("Error fulfilling read request.  Read " + bytesRead
                    + " bytes of " + out.length + ".");
        }
        
		if (protocolHandler != null) {
			byte[] unwrappedIoOut = protocolHandler.unwrapResponse(ioOut);
			if (unwrappedIoOut.length != out.length) {
				throw new IOException(
						"Error fulfilling the read request.  The unwrapped payload of length "
								+ unwrappedIoOut.length + " does not match the expected length of "
								+ out.length + ".");
			}

			System.arraycopy(unwrappedIoOut, 0, out, 0, unwrappedIoOut.length);
		}
        
        result.result = out;
        result.requestEndTime = System.currentTimeMillis();
        
        return result;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
