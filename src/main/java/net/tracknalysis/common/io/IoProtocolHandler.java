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

/**
 * Interface for simple protocol handlers that decorate request and response
 * payloads with extra protocol data
 * 
 * @author David Valeri
 */
public interface IoProtocolHandler {

	/**
	 * Wrap raw request payload data in the protocol wrapper.
	 * 
	 * @param raw
	 *            the raw payload
	 * 
	 * @return the wrapped version of the raw payload
	 */
	byte[] wrapRequest(byte[] raw) throws IOException;
	
	/**
	 * Returns the wrapped length of a request message of length {@code length}
	 * 
	 * @param length
	 *            the unwrapped length of the message
	 * @return the wrapped length of a message of length {@code length}
	 */
	int getWrappedRequestLength(int length) throws IOException;
	
	/**
	 * Unwrap raw response payload data from the protocol wrapper.
	 * 
	 * @param raw
	 *            the wrapped payload
	 * 
	 * @return the raw version of the wrapped payload
	 */
	byte[] unwrapResponse(byte[] wrapped) throws IOException;

	/**
	 * Returns the wrapped length of a response message of length {@code length}
	 * 
	 * @param length
	 *            the unwrapped length of the message
	 * @return the wrapped length of a message of length {@code length}
	 */
	int getWrappedResponseLength(int length) throws IOException;
}
