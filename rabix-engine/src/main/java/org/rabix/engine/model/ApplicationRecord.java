package org.rabix.engine.model;

import org.rabix.bindings.model.Application;

public class ApplicationRecord {

  private String id;
  private Application app;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Application getApp() {
    return app;
  }

  public void setApp(Application app) {
    this.app = app;
  }

}
