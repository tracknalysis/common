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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

/**
 * Simulator that can replay/validate communications based on the contents of a debug log.
 *
 * @author David Valeri
 *
 * @see DebugLogWriterIoManager
 */
public class DebugLogReaderIoManager implements IoManager {

    private final Base64 codec = new Base64();
    private final BufferedReader reader;
    
    public DebugLogReaderIoManager(InputStream logFileInputStream) throws IOException {
        reader = new BufferedReader(new InputStreamReader(logFileInputStream)); 
    }
    
    @Override
    public void connect() throws IOException {
        // No-op
    }

    @Override
    public void disconnect() throws IOException {
        // No-op
    }
    
    @Override
    public IoManagerResult write(byte[] command) throws IOException {
        return writeInternal(command, null);
    }
    
    @Override
    public IoManagerResult write(byte[] command,
    		IoProtocolHandler protocolHandler) throws IOException {
    	return writeInternal(command, protocolHandler);
    }

    @Override
    public IoManagerResult writeAndRead(byte[] command, long delay) throws IOException {
        return writeAndReadInternal(command, delay, null);
    }
    
    @Override
    public IoManagerResult writeAndRead(byte[] command, long delay,
    		IoProtocolHandler protocolHandler) throws IOException {
    	return writeAndReadInternal(command, delay, protocolHandler);
    }

    @Override
    public IoManagerResult writeAndRead(byte[] command, byte[] result, long timeout) throws IOException {
        return writeAndRead(command, result, timeout, null);
    }
    
    @Override
    public IoManagerResult writeAndRead(byte[] command, byte[] out,
    		long timeout, IoProtocolHandler protocolHandler) throws IOException {
    	
    	IoManagerResult result = writeAndReadInternal(command, timeout/10, protocolHandler);
        if (result.result.length != out.length) {
            throw new IOException("Expected [" + out.length
                    + "] result bytes but have [" + result.result.length
                    + "] bytes");
        } else {
            System.arraycopy(result.result, 0, out, 0, result.result.length);
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
    	return readInternal(0, protocolHandler);
    }

    @Override
    public IoManagerResult read(byte[] result, long timeout) throws IOException {
        return read(result, timeout, null);
    }
    
    @Override
    public IoManagerResult read(byte[] out, long timeout,
    		IoProtocolHandler protocolHandler) throws IOException {
    	IoManagerResult result = readInternal(timeout/10, protocolHandler);
        
        if (result.result.length != out.length) {
            throw new IOException("Expected [" + out.length
                    + "] result bytes but have [" + result.result.length
                    + "] bytes");
        } else {
        	System.arraycopy(result.result, 0, out, 0, result.result.length);
        }
        
        return result;
    }

    @Override
    public void flushAll() throws IOException {
        // Do nothing for this in simulation
    }
    
    private void delay(long time) throws IOException {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while delaying.", e);
        }
    }
    
	private IoManagerResult writeInternal(byte[] command,
			IoProtocolHandler protocolHandler) throws IOException {
    	IoManagerResult result = new IoManagerResult();
    	result.requestStartTime = System.currentTimeMillis();
    	result.requestTxStartTime = System.currentTimeMillis();
    	
        String line = reader.readLine();
        
        byte[] protoCommand = command;
        if (protocolHandler != null) {
        	protoCommand = protocolHandler.wrapRequest(command);
        }
        
        if (DebugLogWriterIoManager.WRITE_COMMENT.equals(line)) {
            String outLine = reader.readLine();
            
            byte[] controlCommand = lineToBytes(outLine);
            
            if (!Arrays.equals(controlCommand, protoCommand)) {
                throw new IOException("Command [" + protoCommand
                        + "] not equal to expected command ["
                        + controlCommand + "].");
            }
        } else {
            throw new IOException("The line didn't start with ["
                    + DebugLogWriterIoManager.WRITE_COMMENT
                    + "].  Line was [" + line + "]");
        }
        
        result.requestTxEndTime = System.currentTimeMillis();;
    	result.requestRxStartTime = result.requestTxEndTime;
    	result.requestRxEndTime = result.requestTxEndTime;
    	result.requestEndTime = System.currentTimeMillis();
        
        return result;
    }
    
	private IoManagerResult writeAndReadInternal(byte[] command, long delay,
			IoProtocolHandler protocolHandler) throws IOException {
		
    	IoManagerResult result = new IoManagerResult();
    	result.requestStartTime = System.currentTimeMillis();
    	
    	
    	
    	String line = reader.readLine();
        
        byte[] protoCommand = command;
        if (protocolHandler != null) {
        	protoCommand = protocolHandler.wrapRequest(command);
        }
        
        result.requestTxStartTime = System.currentTimeMillis();
    	
        if (DebugLogWriterIoManager.WRITE_AND_READ_COMMENT.equals(line)) {
            String outLine = reader.readLine();
            
            byte[] controlCommand = lineToBytes(outLine);
            
            if (!Arrays.equals(controlCommand, protoCommand)) {
                throw new IOException("Command [" + protoCommand
                        + "] not equal to expected command ["
                        + controlCommand + "].");
            } else {
            	result.requestTxEndTime = System.currentTimeMillis();
            	delay(delay);
            	result.requestRxStartTime = System.currentTimeMillis();
            	result.result = lineToBytes(reader.readLine());
            	result.requestRxEndTime = System.currentTimeMillis();
                
                if (protocolHandler != null) {
                	result.result = protocolHandler.unwrapResponse(result.result);
                }
            }
            
        } else {
            throw new IOException("The line didn't start with ["
                    + DebugLogWriterIoManager.WRITE_AND_READ_COMMENT
                    + "].  Line was [" + line + "]");
        }
        
        result.setRequestEndTime(System.currentTimeMillis());
        
        return result;
    }
    
    private IoManagerResult readInternal(long delay, IoProtocolHandler protocolHandler) throws IOException {
    	IoManagerResult result = new IoManagerResult();
    	result.result = new byte[0];
    	result.requestStartTime = System.currentTimeMillis();
    	result.requestTxStartTime = result.requestStartTime;
    	result.requestTxEndTime = result.requestStartTime;
    	
    	result.requestRxStartTime = System.currentTimeMillis();
    	delay(delay);
    	
    	String line = reader.readLine();
        
        if (DebugLogWriterIoManager.READ_COMMENT.equals(line)) {
        	
        	result.result = lineToBytes(reader.readLine());
        	result.requestRxEndTime = result.requestRxStartTime + delay / 2;
        	
        	if (protocolHandler != null) {
        		result.result = protocolHandler.unwrapResponse(result.result);
        	}
        } else {
            throw new IOException("The line didn't start with ["
                    + DebugLogWriterIoManager.READ_COMMENT
                    + "].  Line was [" + line + "]");
        }
        
        result.setRequestEndTime(System.currentTimeMillis());
        
        return result;
    }
    
    private byte[] lineToBytes(String line) throws IOException {
        if (line == null) {
            throw new IOException();
        } else {
            byte[] lineBase64Bytes = line.getBytes("UTF-8");
            return codec.decode(lineBase64Bytes);
        }
    }
}
