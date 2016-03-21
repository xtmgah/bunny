package org.rabix.bindings.model;

public enum LinkMerge {
  merge_nested,
  merge_flattened;
  
  public static boolean isBlocking(LinkMerge linkMerge) {
    switch (linkMerge) {
    case merge_nested:
      return false;
    case merge_flattened:
      return true;
    default:
      return true;
    }
  }
  
}
