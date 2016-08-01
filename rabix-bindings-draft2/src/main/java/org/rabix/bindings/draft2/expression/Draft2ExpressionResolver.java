package org.rabix.bindings.draft2.expression;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2ExpressionEngineRequirement;
import org.rabix.bindings.draft2.expression.javascript.Draft2ExpressionJavascriptResolver;
import org.rabix.bindings.draft2.expression.jsonpointer.Draft2ExpressionJSPointerResolver;

public class Draft2ExpressionResolver {

  public static String KEY_REQUIREMENT_ID = "id";
  public static String KEY_EXPRESSION_CONFIG = "engineConfig";

  public static final String JSON_POINTER_ENGINE = "cwl:JsonPointer";

  public static <T> T evaluate(final String expression) throws Draft2ExpressionException {
    return evaluate(expression, null, null, null);
  }

  public static <T> T evaluate(final String expression, boolean includeTemplates) throws Draft2ExpressionException {
    return evaluate(expression, null, null, includeTemplates);
  }

  public static <T> T evaluate(final String expression, final Draft2Job context, final Object self)
      throws Draft2ExpressionException {
    return evaluate(expression, context, self, false);
  }

  public static <T> T evaluate(final String expression, final Draft2Job context, final Object self, boolean includeTemplates)
      throws Draft2ExpressionException {
    return evaluate(expression, context, self, includeTemplates, null);
  }

  public static <T> T evaluate(final String expression, final Draft2Job context, final Object self, String language)
      throws Draft2ExpressionException {
    return evaluate(expression, context, self, false, language);
  }

  @SuppressWarnings({ "unchecked" })
  public static <T> T evaluate(final String expression, final Draft2Job context, final Object self, boolean includeTemplates,
      String language) throws Draft2ExpressionException {
    if (language == null) {
      language = Draft2ExpressionLanguage.JAVASCRIPT.getDefault(); // assume it's JavaScript
    }

    Object transformedContext = transformContext(context, language);
    Draft2ExpressionLanguage expressionLanguage = Draft2ExpressionLanguage.convert(language);
    switch (expressionLanguage) {
    case JSON_POINTER:
      return (T) Draft2ExpressionJSPointerResolver.evaluate(transformedContext, self, expression);
    default:
      List<String> engineConfigs = fetchEngineConfigs(context, language);
      return (T) Draft2ExpressionJavascriptResolver.evaluate(transformedContext, self, expression, engineConfigs, includeTemplates);
    }
  }

  /**
   * By reference CWL implementation, context is equals to 'inputs' section from the Job
   * Out implementation uses whole Job as a context
   */
  private static Object transformContext(Draft2Job context, String language) {
    if (context == null) {
      return null;
    }

    if (!language.equals("#cwl-js-engine") && !language.equals("cwl-js-engine")) {
      return context.getInputs();
    }
    return context;
  }

  /**
   * Fetch engine configuration from requirements
   */
  private static List<String> fetchEngineConfigs(Draft2Job context, String language) {
    if (context == null) {
      return null;
    }
    List<String> result = new ArrayList<>();
    List<Draft2ExpressionEngineRequirement> requirements = context.getApp().getExpressionEngineRequirements();
    if (requirements != null) {
      for (Draft2ExpressionEngineRequirement requirement : requirements) {
        List<String> engineConfiguration = requirement.getEngineConfigs(language);
        if (engineConfiguration != null) {
          result.addAll(engineConfiguration);
        }
      }
    }
    return result;
  }

}