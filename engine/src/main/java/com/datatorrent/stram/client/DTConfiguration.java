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
package com.datatorrent.stram.client;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.lang3.BooleanUtils;

import com.datatorrent.api.StreamingApplication;
import com.datatorrent.stram.plan.logical.LogicalPlan;
import com.datatorrent.stram.plan.logical.LogicalPlanConfiguration;

/**
 * <p>
 * DTConfiguration class</p>
 *
 * @since 0.9.4
 */
public class DTConfiguration implements Iterable<Map.Entry<String, String>>
{
  public enum Scope
  {
    GLOBAL, LOCAL, USER, TRANSIENT
  }

  private final Map<String, ValueEntry> map = new LinkedHashMap<>();
  private static final Logger LOG = LoggerFactory.getLogger(DTConfiguration.class);

  public static class ValueEntry
  {
    public String value;
    public boolean isFinal = false;
    public Scope scope = Scope.TRANSIENT;
    public String description;
  }

  public static class ConfigException extends Exception
  {
    private static final long serialVersionUID = 1L;

    public ConfigException(String message)
    {
      super(message);
    }

  }

  @Override
  public Iterator<Entry<String, String>> iterator()
  {
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, ValueEntry> entry : map.entrySet()) {
      result.put(entry.getKey(), entry.getValue().value);
    }
    return result.entrySet().iterator();
  }

  public void writeToFile(File file, Scope scope, String comment) throws IOException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Date date = new Date();
    Document doc;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException ex) {
      throw new RuntimeException(ex);
    }
    Element rootElement = doc.createElement("configuration");
    rootElement.appendChild(doc.createComment(" WARNING: Do not edit this file. Your changes will be overwritten. "));
    rootElement.appendChild(doc.createComment(" Written by dtgateway on " + sdf.format(date)));
    rootElement.appendChild(doc.createComment(" " + comment + " "));
    doc.appendChild(rootElement);
    for (Map.Entry<String, ValueEntry> entry : map.entrySet()) {
      ValueEntry valueEntry = entry.getValue();
      if (scope == null || valueEntry.scope == scope) {
        Element property = doc.createElement("property");
        rootElement.appendChild(property);
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(entry.getKey()));
        property.appendChild(name);
        Element value = doc.createElement("value");
        value.appendChild(doc.createTextNode(valueEntry.value));
        property.appendChild(value);
        if (valueEntry.description != null) {
          Element description = doc.createElement("description");
          description.appendChild(doc.createTextNode(valueEntry.description));
          property.appendChild(description);
        }
        if (valueEntry.isFinal) {
          Element isFinal = doc.createElement("final");
          isFinal.appendChild(doc.createTextNode("true"));
          property.appendChild(isFinal);
        }
      }
    }
    rootElement.appendChild(doc.createComment(" WARNING: Do not edit this file. Your changes will be overwritten. "));

    // write the content into xml file
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(file);
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(source, result);
    } catch (TransformerConfigurationException ex) {
      throw new RuntimeException(ex);
    } catch (TransformerException ex) {
      throw new IOException(ex);
    }
  }

  public void writeToFile(File file, String comment) throws IOException
  {
    writeToFile(file, null, comment);
  }

  public void loadFile(File file, Scope defaultScope) throws IOException, ParserConfigurationException, SAXException, ConfigException
  {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    Element documentElement = doc.getDocumentElement();
    if (!documentElement.getNodeName().equals("configuration")) {
      throw new ConfigException("Root element needs to be \"configuration\"");
    }
    if (doc.hasChildNodes()) {
      NodeList propertyNodes = documentElement.getChildNodes();
      for (int i = 0; i < propertyNodes.getLength(); i++) {
        Node propertyNode = propertyNodes.item(i);
        if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
          if (propertyNode.getNodeName().equals("property")) {
            processPropertyNode((Element)propertyNode, defaultScope);
          } else {
            LOG.warn("Ignoring unknown element {}", propertyNode.getNodeName());
          }
        }
      }
    }
  }

  public void loadFile(File file) throws IOException, ParserConfigurationException, SAXException, ConfigException
  {
    loadFile(file, Scope.TRANSIENT);
  }

  private void processPropertyNode(Element propertyNode, Scope defaultScope)
  {
    NodeList nodeList = propertyNode.getElementsByTagName("name");
    if (nodeList.getLength() == 0 || nodeList.item(0).getTextContent().isEmpty()) {
      LOG.warn("Name element not found, ignoring property entry");
      return;
    }
    String name = nodeList.item(0).getTextContent().trim();
    ValueEntry valueEntry = new ValueEntry();
    nodeList = propertyNode.getElementsByTagName("value");
    if (nodeList.getLength() == 0) {
      valueEntry.value = null;
    } else {
      valueEntry.value = nodeList.item(0).getTextContent().trim();
    }
    nodeList = propertyNode.getElementsByTagName("description");
    if (nodeList.getLength() > 0) {
      valueEntry.description = nodeList.item(0).getTextContent().trim();
    }
    nodeList = propertyNode.getElementsByTagName("final");
    if (nodeList.getLength() > 0) {
      valueEntry.isFinal = BooleanUtils.toBoolean(nodeList.item(0).getTextContent());
    }
    valueEntry.scope = defaultScope;

    //if (!name.startsWith(StreamingApplication.DT_PREFIX)) {
    //  LOG.warn("Name {} does not start with \"dt.\", ignoring property entry", name);
    //  return;
    //}
    if (map.containsKey(name) && map.get(name).isFinal) {
      LOG.warn("Trying to override final property {}, ignoring property entry", name);
      return;
    }

    map.put(name, valueEntry);
  }

  public boolean containsKey(String key)
  {
    return map.containsKey(key);
  }

  public String get(String key)
  {
    ValueEntry value = map.get(key);
    return (value == null) ? null : value.value;
  }

  public String getDescription(String key)
  {
    ValueEntry value = map.get(key);
    return (value == null) ? null : value.description;
  }

  public Scope getScope(String key)
  {
    ValueEntry value = map.get(key);
    return (value == null) ? Scope.TRANSIENT : value.scope;
  }

  public Integer getInteger(String key)
  {
    String value = get(key);
    return (value == null) ? null : Integer.valueOf(value);
  }

  public Long getLong(String key)
  {
    String value = get(key);
    return (value == null) ? null : Long.valueOf(value);
  }

  public Float getFloat(String key)
  {
    String value = get(key);
    return (value == null) ? null : Float.valueOf(value);
  }

  public Double getDouble(String key)
  {
    String value = get(key);
    return (value == null) ? null : Double.valueOf(value);
  }

  public void remove(String key)
  {
    map.remove(key);
  }

  public ValueEntry setInternal(String key, String value)
  {
    ValueEntry valueEntry;
    if (map.containsKey(key)) {
      valueEntry = map.get(key);
      valueEntry.value = value;
    } else {
      valueEntry = new ValueEntry();
      valueEntry.scope = isLocalKey(key) ? Scope.LOCAL : Scope.TRANSIENT;
      map.put(key, valueEntry);
    }
    return valueEntry;
  }

  public ValueEntry set(String key, String value, Scope scope, String description) throws ConfigException
  {
    ValueEntry valueEntry;
    if (map.containsKey(key)) {
      valueEntry = map.get(key);
      if (valueEntry.isFinal) {
        throw new ConfigException("Cannot set final property " + key);
      }
    } else {
      valueEntry = new ValueEntry();
    }
    valueEntry.value = value;
    valueEntry.description = description;
    valueEntry.scope = isLocalKey(key) ? Scope.LOCAL : scope;
    map.put(key, valueEntry);
    return valueEntry;
  }

  public static boolean isLocalKey(String key)
  {
    return key.equals(StramClientUtils.DT_DFS_ROOT_DIR)
        || key.equals(LogicalPlanConfiguration.GATEWAY_LISTEN_ADDRESS)
        || key.equals(StramClientUtils.DT_CONFIG_STATUS)
        || key.equals(StramClientUtils.DT_VERSION)
        || key.equals(StreamingApplication.DT_PREFIX + LogicalPlan.GATEWAY_CONNECT_ADDRESS.getName());
  }

  public JSONObject toJSONObject()
  {
    JSONObject json = new JSONObject();
    for (Map.Entry<String, ValueEntry> entry : map.entrySet()) {
      try {
        JSONObject jsonValue = new JSONObject();
        jsonValue.put("value", entry.getValue().value);
        if (entry.getValue().description != null) {
          jsonValue.put("description", entry.getValue().description);
        }
        jsonValue.put("scope", entry.getValue().scope);
        json.put(entry.getKey(), jsonValue);
      } catch (JSONException ex) {
        // should not happen here
        throw new RuntimeException(ex);
      }
    }
    return json;
  }

  public JSONObject toJSONObject(String key)
  {
    JSONObject json = new JSONObject();
    ValueEntry valueEntry = map.get(key);
    if (valueEntry != null) {
      try {
        json = new JSONObject();
        json.put("value", valueEntry.value);
        if (valueEntry.description != null) {
          json.put("description", valueEntry.description);
        }
        if (valueEntry.scope == Scope.LOCAL) {
          json.put("scope", valueEntry.scope);
        }
      } catch (JSONException ex) {
        // should not happen here
        throw new RuntimeException(ex);
      }
      return json;
    } else {
      return null;
    }
  }

}
