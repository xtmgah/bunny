package org.rabix.bindings.protocol.draft3.expression;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3ExpressionEngineRequirement;
import org.rabix.bindings.protocol.draft3.expression.javascript.Draft3ExpressionJavascriptResolver;

public class Draft3ExpressionResolver {

  public static String KEY_REQUIREMENT_ID = "id";
  public static String KEY_EXPRESSION_CONFIG = "engineConfig";

  public static final String JSON_POINTER_ENGINE = "cwl:JsonPointer";

  public static <T> T evaluate(final String expression) throws Draft3ExpressionException {
    return evaluate(expression, null, null, null);
  }

  public static <T> T evaluate(final String expression, boolean includeTemplates) throws Draft3ExpressionException {
    return evaluate(expression, null, null, includeTemplates);
  }

  public static <T> T evaluate(final String expression, final Draft3Job context, final Object self)
      throws Draft3ExpressionException {
    return evaluate(expression, context, self, false);
  }

  public static <T> T evaluate(final String expression, final Draft3Job context, final Object self, boolean includeTemplates)
      throws Draft3ExpressionException {
    return evaluate(expression, context, self, includeTemplates, null);
  }

  public static <T> T evaluate(final String expression, final Draft3Job context, final Object self, String language)
      throws Draft3ExpressionException {
    return evaluate(expression, context, self, false, language);
  }

  @SuppressWarnings({ "unchecked" })
  public static <T> T evaluate(final String expression, final Draft3Job context, final Object self, boolean includeTemplates,
      String language) throws Draft3ExpressionException {
    if (language == null) {
      language = Draft3ExpressionLanguage.JAVASCRIPT.getDefault(); // assume it's JavaScript
    }

    Object transformedContext = transformContext(context, language);
    List<String> engineConfigs = fetchEngineConfigs(context, language);
    return (T) Draft3ExpressionJavascriptResolver.evaluate(transformedContext, self, expression, engineConfigs, includeTemplates);
  }

  /**
   * By reference CWL implementation, context is equals to 'inputs' section from the Job
   * Out implementation uses whole Job as a context
   */
  private static Object transformContext(Draft3Job context, String language) {
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
  private static List<String> fetchEngineConfigs(Draft3Job context, String language) {
    if (context == null) {
      return null;
    }
    List<String> result = new ArrayList<>();
    List<Draft3ExpressionEngineRequirement> requirements = context.getApp().getExpressionEngineRequirements();
    if (requirements != null) {
      for (Draft3ExpressionEngineRequirement requirement : requirements) {
        List<String> engineConfiguration = requirement.getEngineConfigs(language);
        if (engineConfiguration != null) {
          result.addAll(engineConfiguration);
        }
      }
    }
    return result;
  }

}