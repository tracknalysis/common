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
 * Interface for a registry of listeners.
 *
 * @author David Valeri
 *
 * @param <T> the type of the listener
 */
public interface ListenerRegistry<T> {

	/**
	 * Adds a strong reference to a listener for notifications. Does nothing if
	 * the listener is already registered through this call.  Upon registration
	 * the previous listener event, if one exists, is triggered on the new listener.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public abstract void addListener(T listener);

	/**
	 * Removes a reference to a listener added through
	 * {@link #addListener(Object)}. Does nothing if the listener was not added
	 * through {@link #addListener(Object)}.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public abstract void removeListener(T listener);

	/**
	 * Adds a weak reference to a listener for notifications. Does nothing if
	 * the listener is already registered through this call. Upon registration
	 * the previous listener event, if one exists, is triggered on the new
	 * listener.The weak reference allows for garbage collection of listeners
	 * that fail to unregister themselves.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public abstract void addWeakReferenceListener(T listener);

	/**
	 * Removes a reference to a listener added through
	 * {@link #addWeakReferenceListener(Object)}. Does nothing if the listener
	 * was not added through {@link #addWeakReferenceListener(Object)}.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public abstract void removeWeakReferenceListener(T listener);
}
