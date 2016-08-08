package org.rabix.bindings.sb.expression.helper;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.SBExpressionResolver;
import org.rabix.bindings.sb.helper.SBSchemaHelper;

public class SBExpressionBeanHelper {

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
      Object type = valueMap.get(SBSchemaHelper.KEY_JOB_TYPE);

      if (type != null && type.equals(SBSchemaHelper.TYPE_JOB_EXPRESSION)) {
        return true;
      }
    }
    return false;
  }

  public static <T> T evaluate(SBJob job, Object expression) throws SBExpressionException {
    return evaluate(job, null, expression);
  }

  @SuppressWarnings("unchecked")
  public static <T> T evaluate(SBJob job, Object self, Object expression) throws SBExpressionException {
    Map<String, Object> expressionMap = (Map<String, Object>) expression;

    String script = MapUtils.getString(expressionMap, KEY_EXPRESSION_VALUE);
    String language = MapUtils.getString(expressionMap, KEY_EXPRESSION_LANGUAGE);
    return SBExpressionResolver.<T> evaluate(script, job, self, language);
  }

}
