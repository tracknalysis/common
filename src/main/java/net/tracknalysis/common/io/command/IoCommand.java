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
package net.tracknalysis.common.io.command;

import net.tracknalysis.common.io.IoManager;

/**
 * Interface for a command that can be queued in a {@link IoCommandManager}.
 *
 * @author David Valeri
 */
public interface IoCommand {
	
	/**
	 * Executes the command against {@code ioManager}.  Any thrown exception
	 * will be logged, but otherwise ignored.  It is the responsibility of
	 * the implementation to manage callbacks for success and failure scenarios.
	 *
	 * @param ioManager the IO manager to use while performing the IO operations
	 */
	void execute(IoManager ioManager);
}
