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
package com.datatorrent.stram.tuple;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import com.datatorrent.bufferserver.packet.MessageType;
import com.datatorrent.stram.api.ConfigurationChange;
import com.datatorrent.stram.api.ConfigurationChangeBatch;

public class ModifyConfigurationTuple extends Tuple
{
  private ConfigurationChangeBatch configurationChangeBatch;
  private Set<String> seenOperators;

  public ModifyConfigurationTuple(ConfigurationChangeBatch configurationChangeBatch, long windowId)
  {
    super(MessageType.MODIFY_CONFIGURATION, windowId);

    Preconditions.checkNotNull(configurationChangeBatch);
    Preconditions.checkArgument(!configurationChangeBatch.isEmpty(), "The given " + ConfigurationChangeBatch.class.getSimpleName() + " cannot be empty.");

    this.configurationChangeBatch = configurationChangeBatch;
    seenOperators = Sets.newHashSet();
  }

  public Collection<ConfigurationChange> remove(String operatorName)
  {
    return configurationChangeBatch.remove(operatorName);
  }

  public Collection<ConfigurationChange> get(String operatorName)
  {
    return configurationChangeBatch.get(operatorName);
  }

  public boolean isEmpty()
  {
    return configurationChangeBatch.isEmpty();
  }

  public void addSeenOperator(String operatorName)
  {
    seenOperators.add(operatorName);
  }

  public boolean sawOperator(String operatorName)
  {
    return seenOperators.contains(operatorName);
  }
}
