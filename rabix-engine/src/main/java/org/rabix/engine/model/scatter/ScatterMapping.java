package org.rabix.engine.model.scatter;

import java.util.List;

import org.rabix.bindings.BindingException;

public interface ScatterMapping {

  void enable(String port, Object value, Integer position);

  void commit(List<RowMapping> mappings);
  
  int getNumberOfRows();
  
  List<RowMapping> getEnabledRows() throws BindingException;
  
}
