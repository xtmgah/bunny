package org.rabix.bindings.protocol.draft3.resolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

public class Draft3DocumentResolver {

  public static final String RESOLVER_REFERENCE_KEY = "import";
  public static final String RESOLVER_JSON_POINTER_KEY = "$job";
  
  private static final String DEFAULT_ENCODING = "UTF-8";

  private JsonNode root;

  private String referenceKey;
  private String jsonPointerKey;

  private class ParentChild {
    JsonNode parent;
    JsonNode child;

    ParentChild(JsonNode parent, JsonNode child) {
      this.parent = parent;
      this.child = child;
    }

    @Override
    public String toString() {
      return "ParentChild [parent=" + parent + ", child=" + child + "]";
    }
  }

  private Set<Draft3DocumentResolverReplacement> replacements;
  private Map<String, Draft3DocumentResolverReference> referenceCache;

  public Draft3DocumentResolver() {
    this.referenceKey = RESOLVER_REFERENCE_KEY;
    this.jsonPointerKey = RESOLVER_JSON_POINTER_KEY;
    this.referenceCache = new HashMap<>();
    this.replacements = new LinkedHashSet<>();
  }

  public String resolve(String app) throws BindingException {
    File file = null;
    try {
      boolean isFile = URIHelper.isFile(app);
      if (isFile) {
        file = new File(URIHelper.getURIInfo(app));
      } else {
        file = new File(".");
      }
      String inputJson = JSONHelper.transformToJSON(URIHelper.getData(app));
      this.root = JSONHelper.readJsonNode(inputJson);
    } catch (IOException e) {
      throw new BindingException(e);
    }
    traverse(file, null, root, true);

    for (Draft3DocumentResolverReplacement replacement : replacements) {
      if (replacement.getParentNode().isArray()) {
        replaceArrayItem(replacement);
      } else if (replacement.getParentNode().isObject()) {
        replaceObjectItem(replacement);
      }
    }
    return JSONHelper.writeObject(root);
  }
  
  private JsonNode traverse(File file, JsonNode parentNode, JsonNode currentNode, boolean addReplacement) throws BindingException {
    Preconditions.checkNotNull(currentNode, "current node id is null");

    boolean isReference = currentNode.has(referenceKey);
    boolean isJsonPointer = currentNode.has(jsonPointerKey) && parentNode != null; // we skip the first level $job

    if (isReference || isJsonPointer) {
      String referencePath = null;
      if (isReference) {
        referencePath = currentNode.get(referenceKey).textValue();
      } else {
        referencePath = currentNode.get(jsonPointerKey).textValue();
      }

      Draft3DocumentResolverReference reference = referenceCache.get(referencePath);
      if (reference != null) {
        if (reference.isResolving()) {
          throw new BindingException("Circular dependency detected!");
        }
      } else {
        reference = new Draft3DocumentResolverReference();
        reference.setResolving(true);
        referenceCache.put(referencePath, reference);

        JsonNode referenceDocumentRoot = findDocumentRoot(file, referencePath, isJsonPointer);
        ParentChild parentChild = findReferencedNode(referenceDocumentRoot, referencePath);
        reference.setResolvedNode(traverse(file, parentChild.parent, parentChild.child, true));
        reference.setResolving(false);
        referenceCache.put(referencePath, reference);
      }
      if (addReplacement) {
        replacements.add(new Draft3DocumentResolverReplacement(parentNode, currentNode, referencePath));
      }
      return reference.getResolvedNode();
    } else if (currentNode.isContainerNode()) {
      for (JsonNode subnode : currentNode) {
        traverse(file, currentNode, subnode, addReplacement);
      }
    }
    return currentNode;
  }

  @SuppressWarnings("deprecation")
  private void replaceObjectItem(Draft3DocumentResolverReplacement replacement) throws BindingException {
    JsonNode parent = replacement.getParentNode() == null ? root : replacement.getParentNode();

    Iterator<Entry<String, JsonNode>> fieldIterator = parent.fields();
    String fieldName = null;
    while (fieldIterator.hasNext()) {
      Entry<String, JsonNode> fieldEntry = fieldIterator.next();
      if (fieldEntry.getValue().equals(replacement.getReferenceNode())) {
        fieldName = fieldEntry.getKey();
        fieldIterator.remove();
        break;
      }
    }
    Draft3DocumentResolverReference reference = referenceCache.get(replacement.getNormalizedReferencePath());
    if (reference != null) {
      ((ObjectNode) parent).put(fieldName, reference.getResolvedNode());
    } else {
      throw new BindingException("Cannot find reference " + replacement.getNormalizedReferencePath());
    }
  }

  private void replaceArrayItem(Draft3DocumentResolverReplacement replacement) throws BindingException {
    JsonNode parent = replacement.getParentNode() == null ? root : replacement.getParentNode();

    Iterator<JsonNode> nodeIterator = parent.elements();
    while (nodeIterator.hasNext()) {
      JsonNode subnode = nodeIterator.next();
      if (subnode.equals(replacement.getReferenceNode())) {
        nodeIterator.remove();
        break;
      }
    }
    if (parent.isArray()) {
      Draft3DocumentResolverReference reference = referenceCache.get(replacement.getNormalizedReferencePath());
      if (reference != null) {
        ((ArrayNode) parent).add(reference.getResolvedNode());
      } else {
        throw new BindingException("Cannot find reference " + replacement.getNormalizedReferencePath());
      }
    }
  }

  private JsonNode findDocumentRoot(File file, String reference, boolean isJsonPointer) throws BindingException {
    JsonNode startNode = root;
    if (isJsonPointer) {
      startNode = startNode.get(jsonPointerKey);
    }
    int start = reference.indexOf("#");

    if (start == 0) {
      return startNode;
    } else {
      String[] parts = reference.split("#");
      if (parts.length > 2) {
        throw new BindingException("Invalid reference " + reference);
      }
      String externalResource = parts[0];
      if (externalResource.startsWith("http")) {
        try {
          URL website = new URL(externalResource);
          URLConnection connection = website.openConnection();
          BufferedReader in = null;

          try {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }

            String linkContents = response.toString();
            return JSONHelper.readJsonNode(linkContents);
          } finally {
            if (in != null) {
              in.close();
            }
          }
        } catch (Exception e) {
          throw new BindingException("Couldn't fetch contents from " + externalResource);
        }
      } else {
        try {
          String filePath = new File(file.getParentFile(), externalResource).getCanonicalPath();
          String fileContents = FileUtils.readFileToString(new File(filePath), DEFAULT_ENCODING);
          return JSONHelper.readJsonNode(JSONHelper.transformToJSON(fileContents));
        } catch (IOException e) {
          throw new BindingException("Couldn't fetch contents from " + externalResource);
        }
      }
    }
  }

  private ParentChild findReferencedNode(JsonNode rootNode, String absolutePath) {
    if (!absolutePath.contains("#")) {
      return new ParentChild(null, rootNode);
    }
    String subpath = absolutePath.substring(absolutePath.indexOf("#") + 1);
    String[] parts = subpath.split("/");

    JsonNode parent = null;
    JsonNode child = rootNode;
    for (String part : parts) {
      if (StringUtils.isEmpty(part)) {
        continue;
      }
      parent = child;
      child = child.get(part);
    }
    return new ParentChild(parent, child);
  }

}
