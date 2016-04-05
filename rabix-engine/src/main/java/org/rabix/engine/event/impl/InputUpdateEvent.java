package org.rabix.engine.event.impl;

import org.rabix.engine.event.Event;

/**
 * This event is used to update one input (per port) for the specific Job. It triggers the algorithm cycle.
 */
public class InputUpdateEvent implements Event {

  private final String jobId;
  private final String contextId;
  
  private final String portId;
  private final Object value;

  private final Integer position;
  private final Integer numberOfScattered;      // number of scattered nodes
  private final boolean eventFromLookAhead;     // it's a look ahead event

  public InputUpdateEvent(String contextId, String jobId, String portId, Object inputValue, Integer position) {
    this.jobId = jobId;
    this.portId = portId;
    this.value = inputValue;
    this.contextId = contextId;
    this.eventFromLookAhead = false;
    this.numberOfScattered = null;
    this.position = position;
  }

  public InputUpdateEvent(String contextId, String jobId, String portId, Object value, boolean eventFromLookAhead, Integer scatteredNodes, Integer position) {
    this.jobId = jobId;
    this.portId = portId;
    this.value = value;
    this.contextId = contextId;
    this.eventFromLookAhead = eventFromLookAhead;
    this.numberOfScattered = scatteredNodes;
    this.position = position;
  }

  public String getJobId() {
    return jobId;
  }

  public String getPortId() {
    return portId;
  }

  public Object getValue() {
    return value;
  }
  
  public Integer getNumberOfScattered() {
    return numberOfScattered;
  }

  public boolean isEventFromLookAhead() {
    return eventFromLookAhead;
  }

  @Override
  public String getContextId() {
    return contextId;
  }
  
  public Integer getPosition() {
    return position;
  }
  
  @Override
  public EventType getType() {
    return EventType.INPUT_UPDATE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
    result = prime * result + (eventFromLookAhead ? 1231 : 1237);
    result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
    result = prime * result + ((portId == null) ? 0 : portId.hashCode());
    result = prime * result + ((numberOfScattered == null) ? 0 : numberOfScattered.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    InputUpdateEvent other = (InputUpdateEvent) obj;
    if (contextId == null) {
      if (other.contextId != null)
        return false;
    } else if (!contextId.equals(other.contextId))
      return false;
    if (eventFromLookAhead != other.eventFromLookAhead)
      return false;
    if (jobId == null) {
      if (other.jobId != null)
        return false;
    } else if (!jobId.equals(other.jobId))
      return false;
    if (portId == null) {
      if (other.portId != null)
        return false;
    } else if (!portId.equals(other.portId))
      return false;
    if (numberOfScattered == null) {
      if (other.numberOfScattered != null)
        return false;
    } else if (!numberOfScattered.equals(other.numberOfScattered))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "InputUpdateEvent [jobId=" + jobId + ", contextId=" + contextId + ", portId=" + portId + ", value=" + value + ", numberOfScattered=" + numberOfScattered + ", eventFromLookAhead=" + eventFromLookAhead + "]";
  }

}
