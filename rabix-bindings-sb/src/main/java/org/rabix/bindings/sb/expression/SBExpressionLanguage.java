package org.rabix.bindings.sb.expression;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public enum SBExpressionLanguage {
  
  JAVASCRIPT(new String[] { "#js", "#cwl-js-engine", "cwl-js-engine", "node-engine.cwl", "#node-engine.cwl" }),
  JSON_POINTER(new String[] { "cwl:JsonPointer", "#cwl:JsonPointer" });

  private String[] languages;
  
  private SBExpressionLanguage(String[] languages) {
    this.languages = languages;
  }

  public static SBExpressionLanguage convert(String lang) throws SBExpressionException {
    Preconditions.checkNotNull(lang);

    lang = StringUtils.trim(lang);
    for (SBExpressionLanguage langEnum : values()) {
      for (String language : langEnum.languages) {
        if (language.compareToIgnoreCase(lang) == 0) {
          return langEnum;
        }
      }
    }
    throw new SBExpressionException("Language " + lang + " is not supported");
  }

  public String getDefault() {
    return "#cwl-js-engine";
  }
  
  public String[] getLanguages() {
    return languages;
  }
}
