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

/**
 * Interface for a {@link NotificationListenerRegistry} that provides facilities for sending notifications
 * to registered listener.
 *
 * @author David Valeri
 *
 * @param <T> the type of the notification
 */
public interface NotificationListenerManager<T extends NotificationType> extends NotificationListenerRegistry<T> {

	public abstract void sendNotification(T notificationType);

	public abstract void sendNotification(T notificationType, Object body);
}