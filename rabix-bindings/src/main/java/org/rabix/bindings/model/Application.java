package org.rabix.bindings.model;

import java.util.List;

public interface Application {

  String serialize();

  List<? extends ApplicationPort> getInputs();
  
  List<? extends ApplicationPort> getOutputs();
}
