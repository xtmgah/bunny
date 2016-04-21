package org.rabix.bindings.protocol.draft3.expression.helper;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.protocol.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.protocol.draft3.helper.Draft3SchemaHelper;

public class Draft3ExpressionBeanHelper {

  public static String KEY_EXPRESSION_VALUE = "script";
  public static String KEY_EXPRESSION_LANGUAGE = "engine";

  public static boolean isExpression(Object value) {
    if (value == null) {
      return false;
    }
    if (value instanceof Map<?, ?>) {
      Map<?, ?> valueMap = (Map<?, ?>) value;
      if (valueMap.containsKey(KEY_EXPRESSION_LANGUAGE) && valueMap.containsKey(KEY_EXPRESSION_VALUE)) {
        return true;
      }
      Object type = valueMap.get(Draft3SchemaHelper.KEY_JOB_TYPE);

      if (type != null && type.equals(Draft3SchemaHelper.TYPE_JOB_EXPRESSION)) {
        return true;
      }
    }
    return false;
  }

  public static <T> T evaluate(Draft3Job job, Object expression) throws Draft3ExpressionException {
    return evaluate(job, null, expression);
  }

  @SuppressWarnings("unchecked")
  public static <T> T evaluate(Draft3Job job, Object self, Object expression) throws Draft3ExpressionException {
    Map<String, Object> expressionMap = (Map<String, Object>) expression;

    String script = MapUtils.getString(expressionMap, KEY_EXPRESSION_VALUE);
    String language = MapUtils.getString(expressionMap, KEY_EXPRESSION_LANGUAGE);
    return Draft3ExpressionResolver.<T> evaluate(script, job, self, language);
  }

}
