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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Simulator that can replay/validate communications based on the contents of a debug log.
 *
 * @author David Valeri
 */
public class DebugLogIoManagerTest {
	
	private ByteArrayOutputStream out;
	private List<byte[]> writes;
	private IoProtocolHandler protocolHandler;
	private byte[] expectedDefaultRead = new byte[10];
	private byte[] expectedUnwrappedDefaultRead = new byte[9];
	
	@Before
	public void setup() {
		out = new ByteArrayOutputStream();
		writes = new LinkedList<byte[]>();
		protocolHandler = new TestProtocolManager();
		Arrays.fill(expectedDefaultRead, (byte) 0xFF);
		Arrays.fill(expectedUnwrappedDefaultRead, (byte) 0xFF);
	}
	
	@Test
	public void testRoundTrip() throws Exception {
	
		
		DebugLogWriterIoManager writer = new DebugLogWriterIoManager(new TestIoManager(), out);
		
		byte[] writeCommand = new byte[] {(byte) 0x00, (byte) 0x01};
		byte[] wrappedWriteCommand = protocolHandler.wrapRequest(writeCommand);
		
		IoManagerResult write1OutResult = writer.write(writeCommand);
		assertNotNull(write1OutResult);
		assertNull(write1OutResult.getResult());
		assertArrayEquals(writeCommand, writes.get(0));
		
		IoManagerResult write2OutResult = writer.write(writeCommand, protocolHandler);
		assertNotNull(write2OutResult);
		assertNull(write2OutResult.getResult());
		assertArrayEquals(writes.get(1), wrappedWriteCommand);
		
		IoManagerResult writeAndRead1OutResult = writer.writeAndRead(writeCommand, 100);
		assertNotNull(writeAndRead1OutResult);
		assertNotNull(writeAndRead1OutResult.getResult());
		assertArrayEquals(expectedDefaultRead, writeAndRead1OutResult.getResult());
		assertArrayEquals(writes.get(2), writeCommand);
		
		IoManagerResult writeAndRead2OutResult = writer.writeAndRead(writeCommand, 100, protocolHandler);
		assertNotNull(writeAndRead2OutResult);
		assertNotNull(writeAndRead2OutResult.getResult());
		assertArrayEquals(expectedUnwrappedDefaultRead, writeAndRead2OutResult.getResult());
		assertArrayEquals(writes.get(3), wrappedWriteCommand);
		
		IoManagerResult writeAndRead3OutResult = writer.writeAndRead(writeCommand, new byte[10], 100);
		assertNotNull(writeAndRead3OutResult);
		assertNotNull(writeAndRead3OutResult.getResult());
		assertArrayEquals(expectedDefaultRead, writeAndRead3OutResult.getResult());
		assertArrayEquals(writes.get(4), writeCommand);
		
		IoManagerResult writeAndRead4OutResult = writer.writeAndRead(writeCommand, new byte[10], 100, protocolHandler);
		assertNotNull(writeAndRead4OutResult);
		assertNotNull(writeAndRead4OutResult.getResult());
		assertArrayEquals(expectedDefaultRead, writeAndRead4OutResult.getResult());
		assertArrayEquals(writes.get(5), wrappedWriteCommand);
		
		IoManagerResult read1OutResult = writer.read();
		assertNotNull(read1OutResult);
		assertNotNull(read1OutResult.getResult());
		assertArrayEquals(expectedDefaultRead, read1OutResult.getResult());
		
		IoManagerResult read2OutResult = writer.read(protocolHandler);
		assertNotNull(read2OutResult);
		assertNotNull(read2OutResult.getResult());
		assertArrayEquals(expectedUnwrappedDefaultRead, read2OutResult.getResult());
		
		IoManagerResult read3OutResult = writer.read(new byte[10], 100);
		assertNotNull(read3OutResult);
		assertNotNull(read3OutResult.getResult());
		assertArrayEquals(expectedDefaultRead, read3OutResult.getResult());
		
		IoManagerResult read4OutResult = writer.read(new byte[10], 100, protocolHandler);
		assertNotNull(read4OutResult);
		assertNotNull(read4OutResult.getResult());
		assertArrayEquals(expectedDefaultRead, read4OutResult.getResult());
		
		//String contents = new String(out.toByteArray());
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		DebugLogReaderIoManager reader = new DebugLogReaderIoManager(in);
		
		IoManagerResult write1InResult = reader.write(writeCommand);
		assertNotNull(write1InResult);
		assertNull(write1OutResult.getResult());
		
		IoManagerResult write2InResult = reader.write(writeCommand, protocolHandler);
		assertNotNull(write2InResult);
		assertNull(write2InResult.getResult());
		
		IoManagerResult writeAndRead1IntResult = reader.writeAndRead(writeCommand, 100);
		assertNotNull(writeAndRead1IntResult);
		assertNotNull(writeAndRead1IntResult.getResult());
		assertArrayEquals(expectedDefaultRead, writeAndRead1IntResult.getResult());
		
		IoManagerResult writeAndRead2IntResult = reader.writeAndRead(writeCommand, 100, protocolHandler);
		assertNotNull(writeAndRead2IntResult);
		assertNotNull(writeAndRead2IntResult.getResult());
		assertArrayEquals(expectedUnwrappedDefaultRead, writeAndRead2IntResult.getResult());
		
		IoManagerResult writeAndRead3InResult = reader.writeAndRead(writeCommand, new byte[10], 100);
		assertNotNull(writeAndRead3InResult);
		assertNotNull(writeAndRead3InResult.getResult());
		assertArrayEquals(expectedDefaultRead, writeAndRead3InResult.getResult());
		
		IoManagerResult writeAndRead4InResult = reader.writeAndRead(writeCommand, new byte[10], 100, protocolHandler);
		assertNotNull(writeAndRead4InResult);
		assertNotNull(writeAndRead4InResult.getResult());
		assertArrayEquals(expectedDefaultRead, writeAndRead4InResult.getResult());
		
		IoManagerResult read1InResult = reader.read();
		assertNotNull(read1InResult);
		assertNotNull(read1InResult.getResult());
		assertArrayEquals(expectedDefaultRead, read1InResult.getResult());
		
		IoManagerResult read2InResult = reader.read(protocolHandler);
		assertNotNull(read2InResult);
		assertNotNull(read2InResult.getResult());
		assertArrayEquals(expectedUnwrappedDefaultRead, read2InResult.getResult());
		
		IoManagerResult read3InResult = reader.read(new byte[10], 100);
		assertNotNull(read3InResult);
		assertNotNull(read3InResult.getResult());
		assertArrayEquals(expectedDefaultRead, read3InResult.getResult());
		
		IoManagerResult read4InResult = reader.read(new byte[10], 100, protocolHandler);
		assertNotNull(read4InResult);
		assertNotNull(read4InResult.getResult());
		assertArrayEquals(expectedDefaultRead, read4InResult.getResult());
	}
	
	private static class TestProtocolManager implements IoProtocolHandler {

		@Override
		public byte[] wrapRequest(byte[] raw) {
			byte[] wrapped = new byte[raw.length + 1];
			wrapped[0] = 0x00;
			System.arraycopy(raw, 0, wrapped, 1, raw.length);
			return wrapped;
		}
		
		@Override
		public int getWrappedRequestLength(int length) throws IOException {
			return length + 1;
		}

		@Override
		public byte[] unwrapResponse(byte[] wrapped) {
			byte[] unwrapped = new byte[wrapped.length - 1];
			System.arraycopy(wrapped, 1, unwrapped, 0, wrapped.length - 1);
			return unwrapped;
		}

		@Override
		public int getWrappedResponseLength(int length) {
			return length + 1;
		}
	}
	
	private class TestIoManager implements IoManager {
		
		@Override
		public void connect() throws IOException {
		}

		@Override
		public void disconnect() throws IOException {
		}

		@Override
		public IoManagerResult write(byte[] command) throws IOException {
			writes.add(command);
			return createResult(true, false, 0, null);
		}

		@Override
		public IoManagerResult write(byte[] command,
				IoProtocolHandler protocolHandler) throws IOException {
			throw new IOException("SHOULD NOT BE CALLED");
		}

		@Override
		public IoManagerResult writeAndRead(byte[] command, long delay)
				throws IOException {
			writes.add(command);
			return createResult(true, true, delay, new byte[10]);
		}

		@Override
		public IoManagerResult writeAndRead(byte[] command, long delay,
				IoProtocolHandler protocolHandler) throws IOException {
			throw new IOException("SHOULD NOT BE CALLED");
		}

		@Override
		public IoManagerResult writeAndRead(byte[] command, byte[] result,
				long timeout) throws IOException {
			writes.add(command);
			return createResult(true, true, timeout/2, result);
		}

		@Override
		public IoManagerResult writeAndRead(byte[] command, byte[] result,
				long timeout, IoProtocolHandler protocolHandler)
				throws IOException {
			throw new IOException("SHOULD NOT BE CALLED");
		}

		@Override
		public IoManagerResult read() throws IOException {
			return createResult(false, true, 0, new byte[10]);
		}

		@Override
		public IoManagerResult read(IoProtocolHandler protocolHandler)
				throws IOException {
			throw new IOException("SHOULD NOT BE CALLED");
		}

		@Override
		public IoManagerResult read(byte[] result, long timeout)
				throws IOException {
			return createResult(false, true, timeout/2, result);
		}

		@Override
		public IoManagerResult read(byte[] result, long timeout,
				IoProtocolHandler protocolHandler) throws IOException {
			throw new IOException("SHOULD NOT BE CALLED");
		}

		@Override
		public void flushAll() throws IOException {			
		}
		
		private IoManagerResult createResult(boolean write, boolean read,
				long delay, byte[] out) throws IOException {
			try {
				IoManagerResult result = new IoManagerResult();
				result.requestStartTime = System.currentTimeMillis();
				if (write) {
					result.requestTxStartTime = System.currentTimeMillis();
					Thread.sleep(100);
					result.requestTxEndTime = System.currentTimeMillis();
				} else {
					result.requestTxStartTime = System.currentTimeMillis();
					result.requestTxEndTime = result.requestTxStartTime;
				}
				
				if (delay > 0) {
					Thread.sleep(delay);
				}
				
				if (read) {
					result.requestRxStartTime = System.currentTimeMillis();
					Thread.sleep(100);
					result.requestRxEndTime = System.currentTimeMillis();
				} else {
					result.requestRxStartTime = System.currentTimeMillis();
					result.requestRxEndTime = result.requestRxStartTime;
				}
				
				if (out != null) {
					Arrays.fill(out, (byte) 0xFF);
					result.result = out;
				}
				
				result.requestEndTime = System.currentTimeMillis();
				
				return result;
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}
}
