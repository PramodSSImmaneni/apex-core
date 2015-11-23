/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.datatorrent.stram.api;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ConfigurationChangeBatch
{
  private Map<String, Collection<ConfigurationChange>> configurationChangeMap;

  public ConfigurationChangeBatch()
  {
  }

  public Collection<ConfigurationChange> remove(String operatorName)
  {
    Preconditions.checkNotNull(operatorName);
    return configurationChangeMap.remove(operatorName);
  }

  public void add(String operatorName, ConfigurationChange configurationChange)
  {
    Preconditions.checkNotNull(operatorName);
    Preconditions.checkNotNull(configurationChange);

    Collection<ConfigurationChange> configurationChanges = configurationChangeMap.get(operatorName);

    if (configurationChanges == null) {
      configurationChanges = Lists.newArrayList();
      configurationChangeMap.put(operatorName, configurationChanges);
    }

    configurationChanges.add(configurationChange);
  }

  public Collection<ConfigurationChange> get(String operatorName)
  {
    Preconditions.checkNotNull(operatorName);
    return configurationChangeMap.get(operatorName);
  }

  public boolean isEmpty()
  {
    return configurationChangeMap.isEmpty();
  }

}
