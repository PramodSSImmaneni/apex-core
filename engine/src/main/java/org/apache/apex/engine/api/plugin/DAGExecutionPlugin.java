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
package org.apache.apex.engine.api.plugin;

import org.apache.apex.api.plugin.Event;
import org.apache.apex.api.plugin.EventType;
import org.apache.apex.api.plugin.Plugin;
import org.apache.hadoop.classification.InterfaceStability;

import com.datatorrent.stram.api.StramEvent;
import com.datatorrent.stram.api.StreamingContainerUmbilicalProtocol;

import static org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEventType.COMMIT_EVENT;
import static org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEventType.HEARTBEAT_EVENT;
import static org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEventType.STRAM_EVENT;

/**
 * DAGExecutionPlugin allows user provided code to respond to various events during the application runtime.
 *
 * Following events are supported
 * <ul>
 *   <li>{@see DAGExecutionPluginContext.HEARTBEAT} The heartbeat from a container is delivered to the plugin after it has been handled by stram</li>
 *   <li>{@see DAGExecutionPluginContext.STRAM_EVENT} All the Stram event generated in Stram will be delivered to the plugin</li>
 *   <li>{@see DAGExecutionPluginContext.COMMIT_EVENT} When committedWindowId changes in the platform an event will be delivered to the plugin</li>
 * </ul>
 *
 */
@InterfaceStability.Evolving
public interface DAGExecutionPlugin extends Plugin<DAGExecutionPluginContext>
{
  enum DAGExecutionEventType implements EventType
  {
    HEARTBEAT_EVENT, STRAM_EVENT, COMMIT_EVENT
  }

  abstract class DAGExecutionEvent<T extends DAGExecutionEventType> extends Event.BaseEvent<T>
  {
    protected DAGExecutionEvent(T eventType)
    {
      super(eventType);
    }

    public static class HeartbeatExecutionEvent extends DAGExecutionEvent<DAGExecutionEventType>
    {
      private final StreamingContainerUmbilicalProtocol.ContainerHeartbeat heartbeat;

      public HeartbeatExecutionEvent(StreamingContainerUmbilicalProtocol.ContainerHeartbeat heartbeat)
      {
        super(HEARTBEAT_EVENT);
        this.heartbeat = heartbeat;
      }

      public StreamingContainerUmbilicalProtocol.ContainerHeartbeat getHeartbeat()
      {
        return heartbeat;
      }
    }

    public static class StramExecutionEvent extends DAGExecutionEvent<DAGExecutionEventType>
    {
      private final StramEvent stramEvent;

      public StramExecutionEvent(StramEvent stramEvent)
      {
        super(STRAM_EVENT);
        this.stramEvent = stramEvent;
      }

      public StramEvent getStramEvent()
      {
        return stramEvent;
      }
    }

    public static class CommitExecutionEvent extends DAGExecutionEvent<DAGExecutionEventType>
    {
      private final long commitWindow;

      public CommitExecutionEvent(long commitWindow)
      {
        super(COMMIT_EVENT);
        this.commitWindow = commitWindow;
      }

      public long getCommitWindow()
      {
        return commitWindow;
      }
    }
  }
}
