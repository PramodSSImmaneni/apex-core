/**
 * Copyright (C) 2015 DataTorrent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.common.util;

import java.io.*;
import java.util.EnumSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datatorrent.netlet.util.DTThrowable;
public class AsyncFSStorageAgent extends FSStorageAgent
{
  private final transient FileSystem fs;
  private final transient Configuration conf;
  private final String localBasePath;

  private boolean syncCheckpoint = false;

  private AsyncFSStorageAgent()
  {
    super();
    fs = null;
    conf = null;
    localBasePath = null;
  }

  public AsyncFSStorageAgent(String path, Configuration conf)
  {
    this(".", path, conf);
  }

  public AsyncFSStorageAgent(String localBasePath, String path, Configuration conf)
  {
    super(path, conf);
    if (localBasePath == null) {
      this.localBasePath = "/tmp";
    }
    else {
      this.localBasePath = localBasePath;
    }
    logger.debug("Initialize storage agent with {}.", this.localBasePath);
    this.conf = conf == null ? new Configuration() : conf;
    try {
      fs = FileSystem.newInstance(this.conf);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void save(final Object object, final int operatorId, final long windowId) throws IOException
  {
    String operatorIdStr = String.valueOf(operatorId);
    File directory = new File(localBasePath, operatorIdStr);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    try (FileOutputStream stream = new FileOutputStream(new File(directory, String.valueOf(windowId)))) {
      store(stream, object);
    }
  }

  public void copyToHDFS(final int operatorId, final long windowId) throws IOException
  {
    String operatorIdStr = String.valueOf(operatorId);
    File directory = new File(localBasePath, operatorIdStr);
    String window = Long.toHexString(windowId);
    Path lPath = new Path(path + Path.SEPARATOR + operatorIdStr + Path.SEPARATOR + TMP_FILE);
    File srcFile = new File(directory, String.valueOf(windowId));
    FSDataOutputStream stream = null;
    boolean stateSaved = false;
    try {
      // Create the temporary file with OverWrite option to avoid dangling lease issue and avoid exception if file already exists
      stream = fileContext.create(lPath, EnumSet.of(CreateFlag.CREATE, CreateFlag.OVERWRITE), Options.CreateOpts.CreateParent.createParent());
      InputStream in = null;
      try {
        in = new FileInputStream(srcFile);
        IOUtils.copyBytes(in, stream, conf, false);
      } finally {
        IOUtils.closeStream(in);
      }
      stateSaved = true;
    } catch (Throwable t) {
      logger.debug("while saving {} {}", operatorId, window, t);
      stateSaved = false;
      DTThrowable.rethrow(t);
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (IOException ie) {
        stateSaved = false;
        throw new RuntimeException(ie);
      } finally {
        if (stateSaved) {
          fileContext.rename(lPath, new Path(path + Path.SEPARATOR + operatorIdStr + Path.SEPARATOR + window), Options.Rename.OVERWRITE);
        }
        FileUtil.fullyDelete(srcFile);
      }
    }
  }

  @Override
  public Object readResolve() throws ObjectStreamException
  {
    return new AsyncFSStorageAgent(this.localBasePath, this.path, null);
  }

  public boolean isSyncCheckpoint()
  {
    return syncCheckpoint;
  }

  public void setSyncCheckpoint(boolean syncCheckpoint)
  {
    this.syncCheckpoint = syncCheckpoint;
  }

  private static final long serialVersionUID = 201507241610L;
  private static final Logger logger = LoggerFactory.getLogger(AsyncFSStorageAgent.class);
}
