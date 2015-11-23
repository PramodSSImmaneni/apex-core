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
package com.datatorrent.stram.configuration;

import com.datatorrent.api.Component;
import com.datatorrent.stram.api.ContainerContext;
import com.datatorrent.stram.api.RequestFactory;
import com.datatorrent.stram.api.StramToNodeModifyConfigurationRequest;
import com.datatorrent.stram.api.StramToNodeSetPropertyRequest;
import com.datatorrent.stram.api.StreamingContainerUmbilicalProtocol;

/**
 *
 */
public class ConfigurationAgent implements Component<ContainerContext>
{
  @Override
  public void setup(ContainerContext context)
  {
    RequestFactory requestFactory = context.getValue(ContainerContext.REQUEST_FACTORY);
    requestFactory.registerDelegate(StreamingContainerUmbilicalProtocol.StramToNodeRequest.RequestType.SET_PROPERTY, new StramToNodeSetPropertyRequest.SetPropertyRequestDelegate());
    requestFactory.registerDelegate(StreamingContainerUmbilicalProtocol.StramToNodeRequest.RequestType.MODIFY_CONFIGURATION, new StramToNodeModifyConfigurationRequest.ModifyConfigurationDelegate());
  }

  @Override
  public void teardown()
  {

  }
}
