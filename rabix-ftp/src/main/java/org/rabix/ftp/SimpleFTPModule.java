package org.rabix.ftp;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SimpleFTPModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SimpleFTPServer.class).in(Scopes.SINGLETON);
  }

}
