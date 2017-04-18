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

import org.apache.hadoop.classification.InterfaceStability.Evolving;

/**
 * The class represents a plugin event that is delivered to plugins to notify them of important system events.
 *
 * Plugins express interest in receiving events by registering handlers for the event type and they handlers receive the
 * events.
 */
@Evolving
public interface Event<T extends EventType>
{
  T getEventType();

  @Evolving
  public class BaseEvent<T extends EventType> implements Event<T>
  {
    private T eventType;

    public BaseEvent(T eventType)
    {
      this.eventType = eventType;
    }

    public T getEventType()
    {
      return eventType;
    }
  }
}
