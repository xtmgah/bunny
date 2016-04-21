package org.rabix.bindings.protocol.draft3.expression;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public enum Draft3ExpressionLanguage {
  
  JAVASCRIPT(new String[] { "#js", "#cwl-js-engine", "cwl-js-engine", "node-engine.cwl", "#node-engine.cwl" }),
  JSON_POINTER(new String[] { "cwl:JsonPointer", "#cwl:JsonPointer" });

  private String[] languages;
  
  private Draft3ExpressionLanguage(String[] languages) {
    this.languages = languages;
  }

  public static Draft3ExpressionLanguage convert(String lang) throws Draft3ExpressionException {
    Preconditions.checkNotNull(lang);

    lang = StringUtils.trim(lang);
    for (Draft3ExpressionLanguage langEnum : values()) {
      for (String language : langEnum.languages) {
        if (language.compareToIgnoreCase(lang) == 0) {
          return langEnum;
        }
      }
    }
    throw new Draft3ExpressionException("Language " + lang + " is not supported");
  }

  public String getDefault() {
    return "#cwl-js-engine";
  }
  
  public String[] getLanguages() {
    return languages;
  }
}
