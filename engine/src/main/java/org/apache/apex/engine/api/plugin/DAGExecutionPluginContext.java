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

import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.apex.api.plugin.PluginContext;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StatsListener.BatchedOperatorStats;
import com.datatorrent.common.util.Pair;
import com.datatorrent.stram.StramAppContext;
import com.datatorrent.stram.util.VersionInfo;
import com.datatorrent.stram.webapp.AppInfo;
import com.datatorrent.stram.webapp.LogicalOperatorInfo;

/**
 * The context for the execution plugins.
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
public interface DAGExecutionPluginContext extends PluginContext
{
  VersionInfo getEngineVersion();

  StramAppContext getApplicationContext();

  AppInfo.AppStats getApplicationStats();

  Configuration getLaunchConfig();

  DAG getDAG();

  String getOperatorName(int id);

  BatchedOperatorStats getPhysicalOperatorStats(int id);

  List<LogicalOperatorInfo> getLogicalOperatorInfoList();

  Queue<Pair<Long, Map<String, Object>>> getWindowMetrics(String operatorName);

  long windowIdToMillis(long windowId);
}
