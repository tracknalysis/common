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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

/**
 * Decorator that adds logging of the bytes read and written in base 64 format.
 *
 * @author David Valeri
 *
 * @see DebugLogReaderIoManager
 */
public class DebugLogWriterIoManager implements IoManager {
    
    public static final String WRITE_COMMENT = "# Write:";
    
    public static final String WRITE_AND_READ_COMMENT = "# Write and read:";
    
    public static final String READ_COMMENT = "# Read:";
    
    private final IoManager delegate;
    private final Base64 codec = new Base64();
    
    private final Writer writer; 
    
    public DebugLogWriterIoManager(IoManager delegate,
            OutputStream outputStream) throws IOException {
        this.delegate = delegate;
        writer = new OutputStreamWriter(new BufferedOutputStream(outputStream));
    }
    
    @Override
    public void connect() throws IOException {
        delegate.connect();   
    }
    
    @Override
    public void disconnect() throws IOException {
        delegate.disconnect();
        writer.flush();
    }

    @Override
    public synchronized IoManagerResult write(byte[] command) throws IOException {
        return write(command, null);
    }
    
    @Override
    public synchronized IoManagerResult write(byte[] command,
    		IoProtocolHandler protocolHandler) throws IOException {
    	
    	byte[] finalCommand = command;
    	if (protocolHandler != null) {
    		finalCommand = protocolHandler.wrapRequest(command);
    	}
    	logWrite(finalCommand);
        return delegate.write(finalCommand);
    }

    @Override
    public synchronized IoManagerResult writeAndRead(byte[] command, long delay) throws IOException {
        return writeAndRead(command, delay, null);
    }
    
    @Override
    public synchronized IoManagerResult writeAndRead(byte[] command, long delay,
    		IoProtocolHandler protocolHandler) throws IOException {
    	
    	byte[] finalCommand = command;
    	if (protocolHandler != null) {
    		finalCommand = protocolHandler.wrapRequest(command);
    	}
    	
    	IoManagerResult result = delegate.writeAndRead(finalCommand, delay);
    	
    	logWriteAndRead(finalCommand, result.result);
    	
    	if (protocolHandler != null) {
    		result.result = protocolHandler.unwrapResponse(result.result);
    	}
    	
    	result.requestEndTime = System.currentTimeMillis();
    	return result;
    }

    @Override
    public synchronized IoManagerResult writeAndRead(byte[] command, byte[] result, long timeout)
            throws IOException {
        return writeAndRead(command, result, timeout, null);
    }
    
    @Override
    public synchronized IoManagerResult writeAndRead(byte[] command, byte[] out,
    		long timeout, IoProtocolHandler protocolHandler) throws IOException {
    	
    	byte[] finalCommand = command;
    	byte[] ioOut = out;
    	if (protocolHandler != null) {
    		finalCommand = protocolHandler.wrapRequest(command);
    		ioOut = new byte[protocolHandler.getWrappedResponseLength(out.length)];
    	}
    	
    	IoManagerResult result = delegate.writeAndRead(finalCommand, ioOut, timeout);
    	
    	logWriteAndRead(finalCommand, result.result);
    	
    	
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
    public synchronized IoManagerResult read() throws IOException {
        return read(null);
    }
    
    @Override
    public synchronized IoManagerResult read(IoProtocolHandler protocolHandler) throws IOException {
    	
    	IoManagerResult result = delegate.read();
    	
    	logRead(result.result);
    	
    	if (protocolHandler != null) {
    		result.result = protocolHandler.unwrapResponse(result.result);
    	}
    	
    	result.requestEndTime = System.currentTimeMillis();
        return result;
    }

    @Override
    public synchronized IoManagerResult read(byte[] result, long timeout) throws IOException {
        return read(result, timeout, null);
    }
    
    @Override
    public IoManagerResult read(byte[] out, long timeout,
    		IoProtocolHandler protocolHandler) throws IOException {
    	
    	byte[] ioOut = out;
    	if (protocolHandler != null) {
    		ioOut = new byte[protocolHandler.getWrappedResponseLength(out.length)];
    	}
    	
    	IoManagerResult result = delegate.read(ioOut, timeout);
    	logRead(result.result);
    	
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
    public synchronized void flushAll() throws IOException {
        delegate.flushAll();
    }
    
    private void logWrite(byte[] bytes) throws IOException {
        writer.write(WRITE_COMMENT);
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytes), Charset.forName("UTF-8")));
        writer.write("\r\n");
        writer.flush();
    }
    
    private void logWriteAndRead(byte[] bytesOut, byte[] bytesIn) throws IOException {
        writer.write(WRITE_AND_READ_COMMENT);
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytesOut), Charset.forName("UTF-8")));
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytesIn), Charset.forName("UTF-8")));
        writer.write("\r\n");
        writer.flush();
    }
    
    private void logRead(byte[] bytes) throws IOException {
        writer.write(READ_COMMENT);
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytes), Charset.forName("UTF-8")));
        writer.write("\r\n");
        writer.flush();
    }
}
