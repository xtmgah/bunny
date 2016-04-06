package org.rabix.bindings.protocol.draft2.expression;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public enum Draft2ExpressionLanguage {
  
  JAVASCRIPT(new String[] { "#js", "#cwl-js-engine", "cwl-js-engine", "node-engine.cwl", "#node-engine.cwl" }),
  JSON_POINTER(new String[] { "cwl:JsonPointer", "#cwl:JsonPointer" });

  private String[] languages;
  
  private Draft2ExpressionLanguage(String[] languages) {
    this.languages = languages;
  }

  public static Draft2ExpressionLanguage convert(String lang) throws Draft2ExpressionException {
    Preconditions.checkNotNull(lang);

    lang = StringUtils.trim(lang);
    for (Draft2ExpressionLanguage langEnum : values()) {
      for (String language : langEnum.languages) {
        if (language.compareToIgnoreCase(lang) == 0) {
          return langEnum;
        }
      }
    }
    throw new Draft2ExpressionException("Language " + lang + " is not supported");
  }

  public String getDefault() {
    return "#cwl-js-engine";
  }
  
  public String[] getLanguages() {
    return languages;
  }
}
