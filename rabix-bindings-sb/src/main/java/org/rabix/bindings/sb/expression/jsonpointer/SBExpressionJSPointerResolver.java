package org.rabix.bindings.sb.expression.jsonpointer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.common.helper.JSONHelper;

/**
 * JsonPointer resolver implementation. Partially supports JsonPointer specification.
 */
public class SBExpressionJSPointerResolver {

  public static final String CONTEXT_NAME = "job";
  public static final String SELF_NAME = "context";
  public static final String ANY_WILDCARD = "*";
  
  
  public static Object evaluate(Object context, Object self, String expr) throws SBExpressionException {
    String trimmedExpr = StringUtils.trim(expr);
    
    Map<String, Object> rootMap = new HashMap<>();
    
    if (context != null) {
      rootMap.put(CONTEXT_NAME, JSONHelper.convertToMap(context));
    }
    if (self != null) {
      if (self instanceof List<?>) {
        rootMap.put(SELF_NAME, JSONHelper.convertToList(self));  
      } else {
        rootMap.put(SELF_NAME, JSONHelper.convertToMap(self));        
      }
    }

    Object result = rootMap;
    String[] exprArray = trimmedExpr.split("/");
    for (String exprArrayPart : exprArray) {
      if (StringUtils.isEmpty(exprArrayPart)) {
        continue;
      }
      result = getElement(expr, result, exprArrayPart);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Object getElement(String expression, Object container, Object key) throws SBExpressionException {
    if (container == null || key == null) {
      throw new SBExpressionException("Failed to resolve JSONPointer expression " + expression);
    }

    if (container instanceof List<?>) {
      List<?> containerList = (List<Object>) container;
      Object result;
      try {
        if (key.equals(ANY_WILDCARD)) {

          if (containerList.isEmpty()) {
            return containerList;
          }

          if (containerList.get(0) instanceof Map<?, ?>) { // only support arrays with elements of the same type
            Map<String, List<Object>> resultMap = new LinkedHashMap<>();
            // convert a list of maps to a map with keys -> list of values
            for (Object val : containerList) {
              Map<String, ?> valueMap = (Map<String, ?>) val;
              for (Map.Entry<String, ?> entry : valueMap.entrySet()) {
                List<Object> valueList = resultMap.get(entry.getKey());
                if (valueList == null) {
                  valueList = new ArrayList<>();
                }
                valueList.add(entry.getValue());
                resultMap.put(entry.getKey(), valueList);
              }
            }
            result = resultMap;
          } else {
            result = containerList;
          }
        } else {
          int keyInt = Integer.parseInt(key.toString());
          if (containerList.size() < keyInt) {
            throw new SBExpressionException("Failed to resolve JSONPointer expression " + expression);
          }
          result = containerList.get(keyInt);
        }
        return result;
      } catch (Exception e) {
        throw new SBExpressionException("Failed to resolve JSONPointer expression " + expression);
      }
    }
    if (container instanceof Map<?, ?>) {
      return ((Map<?, ?>) container).get(key);
    }
    throw new SBExpressionException("Failed to resolve JSONPointer expression " + expression);
  }

}
