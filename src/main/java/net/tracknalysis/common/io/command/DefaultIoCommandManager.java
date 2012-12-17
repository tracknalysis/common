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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tracknalysis.common.concurrent.GracefulShutdownThread;
import net.tracknalysis.common.io.IoManager;

/**
 * Default blocking queue based implementation of a command manager.
 *
 * @author David Valeri
 */
public class DefaultIoCommandManager implements IoCommandManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultIoCommandManager.class);
	private static final AtomicInteger COMMAND_THREAD_INSTANCE_COUNTER = new AtomicInteger();
	
	private final BlockingQueue<IoCommand> commandQueue = new LinkedBlockingDeque<IoCommand>(10);
	private final IoManager ioManager;
	private CommandThread commandThread;
	private volatile boolean running;
	
	public DefaultIoCommandManager(IoManager ioManager) {
		this.ioManager = ioManager;
	}
	
	public synchronized void start() {
		if (!running) {
			commandThread = new CommandThread();
			LOG.info("{}: Starting new command thread: {}.", this, commandThread.getName());
			commandThread.start();
			LOG.info("{}: Starting new command thread: {}.", this, commandThread.getName());
			running = true;
		}
	}
	
	public synchronized void stop() {
		if (running) {
			running = false;
			LOG.info("{}: Stopping command thread: {}.", this, commandThread.getName());
			commandQueue.clear();
			commandThread.cancel();
			LOG.info("{}: Stopped command thread: {}.", this, commandThread.getName());
			commandThread = null;
		}
	}

	@Override
	public synchronized boolean enqueue(IoCommand ioCommand) {
		if (running) {
			return commandQueue.offer(ioCommand);
		} else {
			throw new IllegalArgumentException("The manager is not running.");
		}
	}
	
	private class CommandThread extends GracefulShutdownThread {
		
		public CommandThread() {
			super("CommandThread: " + COMMAND_THREAD_INSTANCE_COUNTER.getAndIncrement());
		}
		
		public void run() {
			IoCommand command = null;
			while (keepRunning()) {
				try {
					command = commandQueue.poll(1000l, TimeUnit.MILLISECONDS);
					if (command == null) {
						LOG.debug(
								"{}: No command received after timeout period.  Polling again...",
								getName());
					} else {
						try {
							LOG.debug("{}: Executing IO command {}.",
									getName(), command);
							command.execute(ioManager);
							LOG.debug("{}: Executed IO command {}.",
									getName(), command);
						} catch (Exception e) {
							LOG.error(getName() + ": Error executing IO command " + command + "." +
									"  Dropping command and proceeding.", e);
						}
					}
				} catch (InterruptedException e) {
					if (keepRunning()) {
						LOG.error(
								getName()
										+ ": Interrupted while waiting for command and still running.",
								e);
					} else {
						LOG.info(
								getName()
										+ ": Interrupted while waiting for command and not running.",
								e);
					}
				}
			}
		}
	}
}
