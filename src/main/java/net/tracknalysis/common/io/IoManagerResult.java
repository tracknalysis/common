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

/**
 * Result structure including timing data and optional output data from a request to an
 * {@link IoManager}.  
 *
 * @author David Valeri
 */
public class IoManagerResult {
	
	protected long requestStartTime;
	protected long requestTxStartTime;
	protected long requestTxEndTime;
	protected long requestRxStartTime;
	protected long requestRxEndTime;
	protected long requestEndTime;
	
	protected byte[] result;

	/**
	 * Returns the start time in milliseconds since the epoch at which
	 * the request processing started.
	 */
	public long getRequestStartTime() {
		return requestStartTime;
	}

	public void setRequestStartTime(long requestStartTime) {
		this.requestStartTime = requestStartTime;
	}

	/**
	 * Returns the start time in milliseconds since the epoch at which
	 * the transmission of any data in the request processing started.
	 */
	public long getRequestTxStartTime() {
		return requestTxStartTime;
	}

	public void setRequestTxStartTime(long requestTxStartTime) {
		this.requestTxStartTime = requestTxStartTime;
	}

	/**
	 * Returns the end time in milliseconds since the epoch at which
	 * the transmission of any data in the request processing concluded.
	 */
	public long getRequestTxEndTime() {
		return requestTxEndTime;
	}

	public void setRequestTxEndTime(long requestTxEndTime) {
		this.requestTxEndTime = requestTxEndTime;
	}

	/**
	 * Returns the start time in milliseconds since the epoch at which
	 * the receiving of any data resulting from the request processing started.
	 */
	public long getRequestRxStartTime() {
		return requestRxStartTime;
	}

	public void setRequestRxStartTime(long requestRxStartTime) {
		this.requestRxStartTime = requestRxStartTime;
	}

	/**
	 * Returns the end time in milliseconds since the epoch at which
	 * the receiving of any data resulting from the request processing concluded.
	 */
	public long getRequestRxEndTime() {
		return requestRxEndTime;
	}

	public void setRequestRxEndTime(long requestRxEndTime) {
		this.requestRxEndTime = requestRxEndTime;
	}

	/**
	 * Returns the end time in milliseconds since the epoch at which
	 * the request processing concluded.
	 */
	public long getRequestEndTime() {
		return requestEndTime;
	}

	public void setRequestEndTime(long requestEndTime) {
		this.requestEndTime = requestEndTime;
	}

	/**
	 * If applicable to the request, returns the output data that resulted from
	 * the request processing.
	 */
	public byte[] getResult() {
		return result;
	}

	public void setResult(byte[] out) {
		this.result = out;
	}
}
