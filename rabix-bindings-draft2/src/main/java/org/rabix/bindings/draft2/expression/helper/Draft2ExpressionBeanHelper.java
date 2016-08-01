package org.rabix.bindings.draft2.expression.helper;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.Draft2ExpressionResolver;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;

public class Draft2ExpressionBeanHelper {

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
      Object type = valueMap.get(Draft2SchemaHelper.KEY_JOB_TYPE);

      if (type != null && type.equals(Draft2SchemaHelper.TYPE_JOB_EXPRESSION)) {
        return true;
      }
    }
    return false;
  }

  public static <T> T evaluate(Draft2Job job, Object expression) throws Draft2ExpressionException {
    return evaluate(job, null, expression);
  }

  @SuppressWarnings("unchecked")
  public static <T> T evaluate(Draft2Job job, Object self, Object expression) throws Draft2ExpressionException {
    Map<String, Object> expressionMap = (Map<String, Object>) expression;

    String script = MapUtils.getString(expressionMap, KEY_EXPRESSION_VALUE);
    String language = MapUtils.getString(expressionMap, KEY_EXPRESSION_LANGUAGE);
    return Draft2ExpressionResolver.<T> evaluate(script, job, self, language);
  }

}
