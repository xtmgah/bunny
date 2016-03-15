package org.rabix.bindings.protocol.draft2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.DocumentReferenceResolver;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

public class Draft2DocumentReferenceResolver implements DocumentReferenceResolver {

  public static final String RESOLVER_REFERENCE_KEY = "import";
  public static final String RESOLVER_JSON_POINTER_KEY = "$job";
  
  private static final String DEFAULT_ENCODING = "UTF-8";

  private JsonNode root;

  private String referenceKey;
  private String jsonPointerKey;

  private class Replacement {
    String normalizedReferencePath;
    JsonNode parentNode = null;
    JsonNode referenceNode = null;
  }

  private class Reference {
    boolean isResolving = true;
    JsonNode resolvedNode = null;
  }

  private class ParentChild {
    JsonNode parent;
    JsonNode child;

    ParentChild(JsonNode parent, JsonNode child) {
      this.parent = parent;
      this.child = child;
    }
  }

  private List<Replacement> replacements;
  private Map<String, Reference> referenceCache;

  public Draft2DocumentReferenceResolver() {
    this.referenceKey = RESOLVER_REFERENCE_KEY;
    this.jsonPointerKey = RESOLVER_JSON_POINTER_KEY;
    this.referenceCache = new HashMap<>();
    this.replacements = new ArrayList<>();
  }

  public String resolve(File file) throws BindingException {
    try {
      String input = FileUtils.readFileToString(file);

      try {
        this.root = JSONHelper.readJsonNodeFromYAML(input);
      } catch (Exception e) {
        this.root = JSONHelper.readJsonNode(input);
      }
    } catch (IOException e) {
      throw new BindingException(e);
    }
    traverse(file, null, root, true);

    for (Replacement replacement : replacements) {
      if (replacement.parentNode.isArray()) {
        replaceArrayItem(replacement);
      } else if (replacement.parentNode.isObject()) {
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

      Reference reference = referenceCache.get(referencePath);
      if (reference != null) {
        if (reference.isResolving) {
          throw new BindingException("Circular dependency detected!");
        }
      } else {
        reference = new Reference();
        referenceCache.put(referencePath, reference);

        JsonNode referenceDocumentRoot = findDocumentRoot(file, referencePath, isJsonPointer);
        ParentChild parentChild = findReferencedNode(referenceDocumentRoot, referencePath);
        reference.resolvedNode = traverse(file, parentChild.parent, parentChild.child, false);
        reference.isResolving = false;
        referenceCache.put(referencePath, reference);
      }
      if (addReplacement) {
        Replacement cachedReference = new Replacement();
        cachedReference.referenceNode = currentNode;
        cachedReference.parentNode = parentNode;
        cachedReference.normalizedReferencePath = referencePath;
        replacements.add(cachedReference);
      }
      return reference.resolvedNode;
    } else if (currentNode.isContainerNode()) {
      for (JsonNode subnode : currentNode) {
        traverse(file, currentNode, subnode, addReplacement);
      }
    }
    return currentNode;
  }

  @SuppressWarnings("deprecation")
  private void replaceObjectItem(Replacement replacement) throws BindingException {
    JsonNode parent = replacement.parentNode == null ? root : replacement.parentNode;

    Iterator<Entry<String, JsonNode>> fieldIterator = parent.fields();
    String fieldName = null;
    while (fieldIterator.hasNext()) {
      Entry<String, JsonNode> fieldEntry = fieldIterator.next();
      if (fieldEntry.getValue().equals(replacement.referenceNode)) {
        fieldName = fieldEntry.getKey();
        fieldIterator.remove();
        break;
      }
    }
    Reference reference = referenceCache.get(replacement.normalizedReferencePath);
    if (reference != null) {
      ((ObjectNode) parent).put(fieldName, reference.resolvedNode);
    } else {
      throw new BindingException("Cannot find reference " + replacement.normalizedReferencePath);
    }
  }

  private void replaceArrayItem(Replacement replacement) throws BindingException {
    JsonNode parent = replacement.parentNode == null ? root : replacement.parentNode;

    Iterator<JsonNode> nodeIterator = parent.elements();
    while (nodeIterator.hasNext()) {
      JsonNode subnode = nodeIterator.next();
      if (subnode.equals(replacement.referenceNode)) {
        nodeIterator.remove();
        break;
      }
    }
    if (parent.isArray()) {
      Reference reference = referenceCache.get(replacement.normalizedReferencePath);
      if (reference != null) {
        ((ArrayNode) parent).add(reference.resolvedNode);
      } else {
        throw new BindingException("Cannot find reference " + replacement.normalizedReferencePath);
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
          return JSONHelper.readJsonNode(fileContents);
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
