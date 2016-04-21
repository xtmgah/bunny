package org.rabix.bindings.protocol.draft3.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3InlineJavascriptRequirement;
import org.rabix.bindings.protocol.draft3.expression.javascript.Draft3ExpressionJavascriptResolver;
import org.rabix.common.helper.JSONHelper;

public class Draft3ExpressionResolver {

  public static String KEY_EXPRESSION_VALUE = "script";
  public static String KEY_EXPRESSION_LANGUAGE = "engine";
  
  private static String segSymbol = "\\w+";
  private static String segSingle = "\\['([^']|\\\')+'\\]";
  private static String segDouble = "\\[\"([^']|\\\")+\"\\]";
  private static String segIndex = "\\[[0-9]+\\]";

  private static String segments = String.format("(.%s|%s|%s|%s)", segSymbol, segSingle, segDouble, segIndex);

  private static String paramRe = String.format("\\$\\((%s)%s*\\)", segSymbol, segments);

  private static Pattern segPattern = Pattern.compile(segments);
  private static Pattern pattern = Pattern.compile(paramRe);
  
  @SuppressWarnings({ "unchecked" })
  public static <T> T evaluate(final Object expression, final Draft3Job job, final Object self) throws Draft3ExpressionException {
    if (expression == null) {
      return null;
    }
    if (isExpressionObject(expression)) {
      String script = (String) ((Map<?, ?>) expression).get(KEY_EXPRESSION_VALUE);
      return (T) Draft3ExpressionJavascriptResolver.evaluate(job.getInputs(), self, script, null);
    }
    if (expression instanceof String) {
      if (job.isInlineJavascriptEnabled()) {
        List<String> expressionLibs = Collections.<String>emptyList();
        Draft3InlineJavascriptRequirement inlineJavascriptRequirement = job.getApp().getInlineJavascriptRequirement();
        if (inlineJavascriptRequirement != null) {
          expressionLibs = inlineJavascriptRequirement.getExpressionLib();
        }
        return (T) Draft3ExpressionJavascriptResolver.evaluate(job.getInputs(), self, (String) expression, expressionLibs);
      } else {
        Map<String, Object> vars = new HashMap<>();
        vars.put("inputs", job.getInputs());
        vars.put("context", self);
        return (T) paramInterpolate((String) expression, vars, true);
      }
    }
    return (T) expression;
  }
  
  private static boolean isExpressionObject(Object expression) {
    return expression instanceof Map<?,?>  && ((Map<?,?>) expression).containsKey(KEY_EXPRESSION_VALUE)  && ((Map<?,?>) expression).containsKey(KEY_EXPRESSION_LANGUAGE);
  }
  
  private static Object nextSegment(String remaining, Object vars) {
    if (!StringUtils.isEmpty(remaining)) {
      Matcher m = segPattern.matcher(remaining);
      if (m.find()) {
        if (m.group(0).startsWith(".")) {
          return nextSegment(remaining.substring(m.end(0)), ((Map<?, ?>) vars).get(m.group(0).substring(1)));
        } else if (m.group(0).charAt(1) == '\"' || m.group(0).charAt(1) == '\'') {
          Character start = m.group(0).charAt(1);
          String key = m.group(0).substring(2, m.group(0).lastIndexOf(start));
          key = key.replace("\\'", "'");
          key = key.replace("\\\"", "\"");
          return nextSegment(remaining.substring(m.end(0)), ((Map<?, ?>) vars).get(key));
        } else {
          String key = m.group(0).substring(1, m.group(0).length() - 1);
          return nextSegment(remaining.substring(m.end(0)), ((Map<?, ?>) vars).get(Integer.parseInt(key)));
        }
      }
    }
    return vars;
  }

  private static Object paramInterpolate(String ex, Map<String, Object> obj, boolean strip) throws Draft3ExpressionException {
    Matcher m = pattern.matcher(ex);
    if (m.find()) {
      Object leaf = nextSegment(m.group(0).substring(m.end(1) - m.start(0), m.group(0).length() - 1), obj.get(m.group(1)));
      if (strip && ex.trim().length() == m.group(0).length()) {
        return leaf;
      } else {
        String leafStr = JSONHelper.writeObject(leaf);
        if (leafStr.startsWith("\"")) {
          leafStr.substring(1, leafStr.length() - 1);
        }
        return ex.substring(0, m.start(0)) + leafStr + paramInterpolate(ex.substring(m.end(0)), obj, false);
      }
    }
    return ex;
  }


}