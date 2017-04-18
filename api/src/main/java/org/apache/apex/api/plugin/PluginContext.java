/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.apex.api.plugin;

import org.apache.hadoop.classification.InterfaceStability;

import com.datatorrent.api.Context;

/**
 * An Apex plugin is user code which runs inside the Apex engine. The interaction between plugin and engine is managed
 * by PluginContext. Plugins can register interest in different events in the engine using the
 * ${@link PluginContext#register(EventType, Plugin.EventHandler)} method.
 */
@InterfaceStability.Evolving
public interface PluginContext extends Context
{

  /**
   * Register interest in an event.
   *
   * Plugins register interest in events using this method. They would need to specify the event type and a handler to
   * handle the event, that would get called when the event occurs. A plugin can register interest in several events but
   * should register only a single handler for any specific event. In case register is called multiple times with the
   * same event type, then the last registered handler will be used.
   *
   * When an event occurs the
   * {@link Plugin.EventHandler#handle(EventType, Object)} method gets called with the event data.
   *
   * @param type The event type
   * @param handler The event handler
   * @param <T> The event payload type
   */
  <T extends EventType, E extends Event<T>> void register(T eventType, Plugin.EventHandler<E> handler);

}
