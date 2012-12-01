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
package net.tracknalysis.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Valeri
 */
public abstract class GracefulShutdownThread extends Thread {
    
    private Logger LOG = LoggerFactory.getLogger(getClass());
    
    private long stopTimeout = 5000;
    private long stopIncrement = 1000;
    
    protected volatile boolean run = true;
    
    public GracefulShutdownThread() {
        super();
    }
    
    public GracefulShutdownThread(Runnable target, String name) {
        super(target, name);
    }

    public GracefulShutdownThread(Runnable target) {
        super(target);
    }

    public GracefulShutdownThread(String name) {
        super(name);
    }

    public GracefulShutdownThread(ThreadGroup group, Runnable target,
            String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    public GracefulShutdownThread(ThreadGroup group, Runnable target,
            String name) {
        super(group, target, name);
    }

    public GracefulShutdownThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public GracefulShutdownThread(ThreadGroup group, String name) {
        super(group, name);
    }

    /**
     * Attempts to gracefully shutdown the thread if it is running.
     *
     * @return true if the thread shutdown
     */
    public synchronized boolean cancel() {
        if (isAlive()) {
            LOG.debug("Attempting graceful shutdown of {} thread.", getName());
            
            run = false;
            
            for (long time = 0; time < stopTimeout && !isAlive(); time += stopIncrement) {
                try {
                	join(stopIncrement);
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted while attempting clean shutdown of {} thread.", getName());
                }
            }
            
            if (isAlive()) {
                LOG.warn("Graceful shutdown of {} thread failed.  Attempting "
                        + "less subtle options.  Expect some error messages to follow.", getName());
            
                interrupt();
                
                try {
                	join(stopIncrement);
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted while attempting clean shutdown of {} thread.", getName());
                }
                
                if (isAlive()) {
                    LOG.error("Forceable shutdown of {} thread failed.  Giving up.", getName());
                } else {
                    LOG.debug("Forceable shutdown of {} thread succeeded.", getName());
                }
            } else {
                LOG.debug("Graceful shutdown of {} thread succeeded.", getName());
            }
        }
        
        return !this.isAlive();
    }
}
