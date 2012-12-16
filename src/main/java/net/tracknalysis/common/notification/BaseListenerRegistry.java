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
package net.tracknalysis.common.notification;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for implementing a {@link ListenerRegistry}.  Provides for management of
 * registrations and invocation of listeners.
 *
 * @author David Valeri
 *
 * @param <T> the type of the listener
 */
public abstract class BaseListenerRegistry<T> implements ListenerRegistry<T> {
	
	private final List<WeakReference<T>> weakNotificationListeners =
            new LinkedList<WeakReference<T>>();
	
	private final List<T> notificationListeners =
            new CopyOnWriteArrayList<T>();
	
	private ListenerInvocationStrategy<T> lastListenerInvocationStrategy;
	private boolean firstInvocation = true;
		
	/**
	 * Strategy class for invoking each listener.
	 * 
	 * @param <T> the type of the listener
	 */
	public static interface ListenerInvocationStrategy<T> {
		void invokeListener(T listener);
	}
	
	@Override
	public final void addListener(T listener) {
    	int index = notificationListeners.indexOf(listener);
        
        if (index == -1) {
        	notificationListeners.add(listener);
            
        	invokeListeners();
        }
	}
    
    @Override
	public final void removeListener(T listener) {
        int index = notificationListeners.indexOf(listener);
            
        if (index > -1) {
        	notificationListeners.remove(index);
        }
    }
    
    @Override
	public final void addWeakReferenceListener(T listener) {
        
        synchronized (weakNotificationListeners) {
            scrubStrategies(weakNotificationListeners);
            
            int index = findInWeakReferenceList(weakNotificationListeners, listener);
            
            if (index == -1) {
            	weakNotificationListeners.add(new WeakReference<T>(listener));
                
            	invokeListeners();
            }
        }
    }
    
    @Override
	public final void removeWeakReferenceListener(T listener) {
        
        synchronized (weakNotificationListeners) {
            scrubStrategies(weakNotificationListeners);
                
            int index = findInWeakReferenceList(weakNotificationListeners, listener);
                
            if (index > -1) {
            	weakNotificationListeners.remove(index);
            }
        }
    }
    
    /**
     * Returns the initial strategy for listener invocation when no previous invocation has
     * occurred.  May be {@code null}.
     */
    protected abstract ListenerInvocationStrategy<T> getInitialListenerInvocationStrategy();
    
    /**
     * Invokes all listeners using the last provided strategy.
     */
    protected final void invokeListeners() {
    	synchronized (this) {
    		invokeListeners(lastListenerInvocationStrategy);
		}
    }
    
    /**
     * Invokes all listeners using the provided strategy.
     */
    protected final void invokeListeners(ListenerInvocationStrategy<T> strategy) {
    	synchronized (this) {
    		ListenerInvocationStrategy<T> strategyToUse = strategy;
    		if (firstInvocation) {
    			if (strategyToUse == null) {
    				strategyToUse = getInitialListenerInvocationStrategy();
    			}
    			
    			firstInvocation = false;
    		}
    		
    		lastListenerInvocationStrategy = strategyToUse;
    		
    		if (lastListenerInvocationStrategy != null) {
    		
	    		synchronized (weakNotificationListeners) {
	            	for (WeakReference<T> ref : weakNotificationListeners) {
	            		if (ref.get() != null) {
	            			lastListenerInvocationStrategy.invokeListener(ref.get());
	            		}
	                }
	            }
	    		
	    		
            	for (T listener : notificationListeners) {
            		lastListenerInvocationStrategy.invokeListener(listener);
                }
    		}
    	}
    }
    
    /**
     * Searches a list of references for a reference that points to {@code listener}.
     *
     * @param listeners the list to search
     * @param listener the listener to look for in the list
     *
     * @return -1 if no matching entry was found or the index of the matching reference in the list
     */
    private int findInWeakReferenceList(
            List<? extends WeakReference<? extends T>> listeners,
            T listener) {
        
        int index = 0;
        
        for (WeakReference<? extends T> existingStrategy : listeners) {
            if (existingStrategy.get() == listener) {
                return index;
            }
            
            index++;
        }
        
        return -1;
    }
    
    /**
     * Culls any references from the list which have had their target garbage collected.
     *
     * @param strategies the list to inspect for stale references
     */
    private void scrubStrategies(
            List<? extends WeakReference<? extends T>> strategies) {
        
        Iterator<? extends WeakReference<? extends T>> iterator =
                strategies.iterator();
        
        while (iterator.hasNext()) {
            WeakReference<?> strategyRef = iterator.next();
            if (strategyRef.get() == null) {
                iterator.remove();
            }
        }
    }
}
