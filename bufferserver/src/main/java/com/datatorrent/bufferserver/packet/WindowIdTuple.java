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
package com.datatorrent.bufferserver.packet;


import com.datatorrent.netlet.util.Slice;
import com.datatorrent.netlet.util.VarInt;

/**
 * <p>WindowIdTuple class.</p>
 *
 * @since 0.3.2
 */
public class WindowIdTuple extends Tuple
{
  public WindowIdTuple(byte[] array, int offset, int length)
  {
    super(array, offset, length);
  }

  @Override
  public int getWindowId()
  {
    return readVarInt(offset + 1, offset + length);
  }

  @Override
  public MessageType getType()
  {
    return MessageType.valueOf(buffer[offset]);
  }

  @Override
  public int getPartition()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Slice getData()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getBaseSeconds()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getWindowWidth()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String toString()
  {
    return "WindowIdTuple{" + getType() + ", " + Integer.toHexString(getWindowId()) + '}';
  }

  public static byte[] getSerializedTuple(int windowId)
  {
    int offset = 1; /* for type */

    int bits = 32 - Integer.numberOfLeadingZeros(windowId);
    offset += bits / 7 + 1;

    byte[] array = new byte[offset];
    VarInt.write(windowId, array, 1);

    return array;
  }

}
