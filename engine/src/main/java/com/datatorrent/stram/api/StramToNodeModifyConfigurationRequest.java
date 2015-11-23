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
package com.datatorrent.stram.api;

import java.io.IOException;
import java.io.Serializable;

import com.datatorrent.api.Operator;
import com.datatorrent.api.StatsListener;
import com.datatorrent.stram.engine.InputNode;
import com.datatorrent.stram.engine.Node;

/**
 *
 */
public class StramToNodeModifyConfigurationRequest extends StreamingContainerUmbilicalProtocol.StramToNodeRequest implements Serializable
{
  private ModifyConfiguration modifyConfiguration;

  public StramToNodeModifyConfigurationRequest()
  {
    requestType = RequestType.MODIFY_CONFIGURATION;
    cmd = null;
  }

  public ModifyConfiguration getModifyConfiguration()
  {
    return modifyConfiguration;
  }

  public void setModifyConfiguration(ModifyConfiguration modifyConfiguration)
  {
    this.modifyConfiguration = modifyConfiguration;
  }

  public static class ModifyConfigurationDelegate implements RequestFactory.RequestDelegate {

    @Override
    public StatsListener.OperatorRequest getRequestExecutor(final Node<?> node,
      final StreamingContainerUmbilicalProtocol.StramToNodeRequest snr)
    {
      return new StatsListener.OperatorRequest()
      {
        @Override
        public StatsListener.OperatorResponse execute(Operator operator, int operatorId, long windowId)
          throws IOException
        {
          ((InputNode)node).addConfigurationChange(((StramToNodeModifyConfigurationRequest)snr).modifyConfiguration);
          return null;
        }
      };
    }
  }

  private static final long serialVersionUID = -519483193483781325L;
}
