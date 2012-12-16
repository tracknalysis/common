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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of {@link NotificationListenerManager}.
 *
 * @author David Valeri
 *
 * @param <T> the type of the notification
 */
public class DefaultNotificationListenerManager<T extends NotificationType> extends
		BaseListenerRegistry<NotificationListener<T>> implements NotificationListenerManager<T> {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationListenerManager.class);
	
	private volatile T lastNotificationType;
    private volatile Object lastNotificationBody;
    
    private final ListenerInvocationStrategy<NotificationListener<T>> listenerInvocationStrategy
    	= new ListenerInvocationStrategy<NotificationListener<T>>() {
			@Override
			public void invokeListener(NotificationListener<T> listener) {
				sendNotificationInternal(listener);
				
			}			
		};
    
    public DefaultNotificationListenerManager(T firstNotificationType, Object firstNotificationBody) {
		lastNotificationType = firstNotificationType;
		lastNotificationBody = firstNotificationBody;
	}
	
	@Override
	public final void sendNotification(T notificationType) {
        sendNotification(notificationType, null);
    }
    
    @Override
	public final void sendNotification(T notificationType, Object body) {
    	synchronized (this) {
    		lastNotificationType = notificationType;
            lastNotificationBody = body;
            
            invokeListeners();
    	}
    }
    
    @Override
    protected ListenerInvocationStrategy<NotificationListener<T>> getInitialListenerInvocationStrategy() {
    	return listenerInvocationStrategy;
    }
    
    /**
     * Sends notifications to all listeners.
     */
    private void sendNotificationInternal(NotificationListener<T> listener) {
    	try {
    		if (listener != null) {
                if (lastNotificationBody != null) {
                    listener.onNotification(lastNotificationType, lastNotificationBody);
                } else {
                    listener.onNotification(lastNotificationType);
                }
            }
        } catch (Exception e) {
            LOG.error("Error in location listener " + listener + ".",
                    e);
        }
    }
}
