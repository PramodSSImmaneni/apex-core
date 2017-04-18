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
package org.apache.apex.engine.plugin;

import java.util.HashMap;
import java.util.Map;

import org.apache.apex.api.plugin.Event;
import org.apache.apex.api.plugin.EventType;
import org.apache.apex.api.plugin.Plugin;
import org.apache.apex.api.plugin.Plugin.EventHandler;

public class PluginInfoMap<T extends Plugin<?>> extends HashMap<T, PluginInfoMap.PluginInfo>
{
  /**
   * Keeps information about plugin and its registrations. Dispatcher use this
   * information while delivering events to plugin.
   */
  public class PluginInfo
  {
    private final T plugin;
    private final Map<EventType, Plugin.EventHandler<?>> registrationMap = new HashMap<>();

    public <E extends Event> void put(EventType eventType, Plugin.EventHandler<E> handler)
    {
      registrationMap.put(eventType, handler);
    }

    public <E extends Event> Plugin.EventHandler<E> get(EventType eventType)
    {
      return (Plugin.EventHandler<E>)registrationMap.get(eventType);
    }

    public PluginInfo(T plugin)
    {
      this.plugin = plugin;
    }

    T getPlugin()
    {
      return plugin;
    }
  }

  PluginInfo getPluginInfo(T plugin)
  {
    PluginInfo pInfo = get(plugin);
    if (pInfo == null) {
      pInfo = new PluginInfo(plugin);
      put(plugin, pInfo);
    }
    return pInfo;
  }

  public <T1 extends EventType, E extends Event<T1>> void register(T1 eventType, Plugin.EventHandler<E> handler, T owner)
  {
    PluginInfoMap.PluginInfo pInfo = getPluginInfo(owner);
    pInfo.put(eventType, handler);
  }

  /*
  public <E> void register(EventType<E> type, EventHandler<E> handler, T owner)
  {
    PluginInfoMap.PluginInfo pInfo = getPluginInfo(owner);
    pInfo.put(type, handler);
  }
  */

  public <T extends EventType> void dispatch(Event<T> e)
  {
    if (!isEmpty()) {
      for (final PluginInfoMap.PluginInfo pInfo : values()) {
        final EventHandler handler = pInfo.get(e.getEventType());
        if (handler != null) {
          handler.handle(e);
        }
      }
    }
  }

}
