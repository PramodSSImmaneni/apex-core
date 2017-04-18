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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.apex.engine.api.plugin.DAGExecutionPlugin;
import org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEvent.CommitExecutionEvent;
import org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEvent.HeartbeatExecutionEvent;
import org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEvent.StramExecutionEvent;
import org.apache.apex.engine.api.plugin.DAGExecutionPluginContext;

import static org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEventType.COMMIT_EVENT;
import static org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEventType.HEARTBEAT_EVENT;
import static org.apache.apex.engine.api.plugin.DAGExecutionPlugin.DAGExecutionEventType.STRAM_EVENT;

public class DebugPlugin implements DAGExecutionPlugin
{
  private int eventCount = 0;
  private int heartbeatCount = 0;
  private int commitCount = 0;
  CountDownLatch latch = new CountDownLatch(3);

  @Override
  public void setup(DAGExecutionPluginContext context)
  {
    context.register(STRAM_EVENT, new EventHandler<StramExecutionEvent>()
    {
      @Override
      public void handle(StramExecutionEvent stramEvent)
      {
        eventCount++;
        latch.countDown();
      }
    });

    context.register(HEARTBEAT_EVENT, new EventHandler<HeartbeatExecutionEvent>()
    {
      @Override
      public void handle(HeartbeatExecutionEvent heartbeatEvent)
      {
        heartbeatCount++;
        latch.countDown();
      }
    });

    context.register(COMMIT_EVENT, new EventHandler<CommitExecutionEvent>()
    {
      @Override
      public void handle(CommitExecutionEvent event)
      {
        commitCount++;
        latch.countDown();
      }
    });
  }

  @Override
  public void teardown()
  {

  }

  public int getEventCount()
  {
    return eventCount;
  }

  public int getHeartbeatCount()
  {
    return heartbeatCount;
  }

  public int getCommitCount()
  {
    return commitCount;
  }

  public void waitForEventDelivery(long timeout) throws InterruptedException
  {
    latch.await(timeout, TimeUnit.SECONDS);
  }
}
