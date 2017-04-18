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
package com.datatorrent.stram.plan.logical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import org.apache.apex.api.plugin.DAGSetupPlugin;
import org.apache.apex.api.plugin.Event;
import org.apache.apex.api.plugin.EventType;
import org.apache.apex.api.plugin.Plugin;
import org.apache.apex.engine.plugin.PluginInfoMap;
import org.apache.apex.engine.plugin.loaders.PropertyBasedPluginLocator;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.Attribute;
import com.datatorrent.api.DAG;

import static org.slf4j.LoggerFactory.getLogger;

public class DAGSetupPluginManager
{
  private static final Logger LOG = getLogger(DAGSetupPluginManager.class);

  private final transient List<DAGSetupPlugin> plugins = new ArrayList<>();
  private Configuration conf;

  public static final String DAGSETUP_PLUGINS_CONF_KEY = "apex.plugin.dag.setup";

  private PluginInfoMap<DAGSetupPlugin> pluginInfoMap = new PluginInfoMap<>();

  private void loadVisitors(Configuration conf)
  {
    this.conf = conf;
    if (!plugins.isEmpty()) {
      return;
    }

    PropertyBasedPluginLocator<DAGSetupPlugin> locator = new PropertyBasedPluginLocator<>(DAGSetupPlugin.class, DAGSETUP_PLUGINS_CONF_KEY);
    this.plugins.addAll(locator.discoverPlugins(conf));
  }

  private class DefaultDAGSetupPluginContext implements DAGSetupPlugin.DAGSetupPluginContext
  {
    private final DAG dag;
    private final Configuration conf;
    private DAGSetupPlugin plugin;

    public DefaultDAGSetupPluginContext(DAG dag, Configuration conf, DAGSetupPlugin plugin)
    {
      this.dag = dag;
      this.conf = conf;
      this.plugin = plugin;
    }

    @Override
    public <T extends EventType, E extends Event<T>> void register(T type, Plugin.EventHandler<E> handler)
    {
      pluginInfoMap.register(type, handler, plugin);
    }

    public DAG getDAG()
    {
      return dag;
    }

    public Configuration getConfiguration()
    {
      return conf;
    }

    @Override
    public Attribute.AttributeMap getAttributes()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getValue(Attribute<T> key)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCounters(Object counters)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendMetrics(Collection<String> metricNames)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  public void setup(DAG dag)
  {
    for (DAGSetupPlugin plugin : plugins) {
      DAGSetupPlugin.DAGSetupPluginContext context = new DefaultDAGSetupPluginContext(dag, conf, plugin);
      plugin.setup(context);
    }
  }

  public void teardown()
  {
    for (DAGSetupPlugin plugin : plugins) {
      plugin.teardown();
    }
  }

  public <T> void dispatch(Event e)
  {
    pluginInfoMap.dispatch(e);
  }

  public static synchronized DAGSetupPluginManager getInstance(Configuration conf)
  {
    DAGSetupPluginManager manager = new DAGSetupPluginManager();
    manager.loadVisitors(conf);
    return manager;
  }
}
