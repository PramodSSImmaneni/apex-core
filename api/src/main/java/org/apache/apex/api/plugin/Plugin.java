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

import com.datatorrent.api.Component;
import com.datatorrent.api.Context;

/**
 * An Apex plugin is user code which runs inside the Apex engine. Plugin implementations implement this interface.
 *
 * Plugins can identify extension points by registering interest in events in the {@link Component#setup(Context)}
 * initialization method. They should also cleanup any additional resources created during shutdown such as helper
 * threads and open files in the {@link Component#teardown()} method.
 * @param <T>
 */
@InterfaceStability.Evolving
public interface Plugin<T extends PluginContext> extends Component<T>
{

  /**
   * A handler that handles an event in the Apex engine. Plugins register interest in events by registering handlers
   * using the PluginContext.
   * @param <E> The event type
   */
  @InterfaceStability.Evolving
  interface EventHandler<E extends Event>
  {
    /**
     * Handle a event.
     *
     * This method is called when the event occurs.
     *
     * @param event
     */
    void handle(E event);
  }
}
