package org.rabix.bindings.protocol.draft3.expression;

import java.util.Collections;
import java.util.List;

import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3InlineJavascriptRequirement;
import org.rabix.bindings.protocol.draft3.expression.javascript.Draft3ExpressionJavascriptResolver;

public class Draft3ExpressionResolver {

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
  public static <T> T evaluate(final String expression, final Draft3Job job, final Object self, boolean includeTemplates, String language) throws Draft3ExpressionException {
    if (language == null) {
      language = Draft3ExpressionLanguage.JAVASCRIPT.getDefault(); // assume it's JavaScript
    }
    List<String> expressionLibs = Collections.<String>emptyList();
    
    Draft3InlineJavascriptRequirement inlineJavascriptRequirement = job.getApp().getInlineJavascriptRequirement();
    if (inlineJavascriptRequirement != null) {
      expressionLibs = inlineJavascriptRequirement.getExpressionLib();
    }
    return (T) Draft3ExpressionJavascriptResolver.evaluate(job.getInputs(), self, expression, expressionLibs, includeTemplates);
  }

}