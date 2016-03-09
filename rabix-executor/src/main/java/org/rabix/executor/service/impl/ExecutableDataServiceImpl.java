package org.rabix.executor.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.model.ExecutableData;
import org.rabix.executor.service.ExecutableDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ExecutableDataServiceImpl implements ExecutableDataService {

  private final static Logger logger = LoggerFactory.getLogger(ExecutableDataServiceImpl.class);

  private final Map<String, Map<String, ExecutableData>> executables = new HashMap<>();

  @Override
  public synchronized ExecutableData find(String id, String contextId) {
    Preconditions.checkNotNull(id);
    logger.debug("find(id={})", id);
    return getExecutables(contextId).get(id);
  }

  @Override
  public synchronized List<ExecutableData> find(ExecutableStatus... statuses) {
    Preconditions.checkNotNull(statuses);

    List<ExecutableStatus> statusList = Arrays.asList(statuses);
    logger.debug("find(status={})", statusList);

    List<ExecutableData> executableDataByStatus = new ArrayList<>();
    for (Entry<String, Map<String, ExecutableData>> entry : executables.entrySet()) {
      for (ExecutableData executableData : entry.getValue().values()) {
        if (statusList.contains(executableData.getStatus())) {
          executableDataByStatus.add(executableData);
        }
      }
    }
    return executableDataByStatus;
  }

  @Override
  public synchronized void save(ExecutableData executableData, String contextId) {
    logger.debug("save(executableData={})", executableData);
    Executable executable = executableData.getExecutable();
    getExecutables(contextId).put(executable.getId(), executableData);
  }

  @Override
  public synchronized void save(ExecutableData executableData, String message, ExecutableStatus status, String contextId) {
    Preconditions.checkNotNull(executableData);
    logger.debug("save(executableData={}, message={}, status={})", executableData, message, status);
    executableData.setStatus(status);
    executableData.setMessage(message);
    save(executableData, contextId);
  }
  
  private synchronized Map<String, ExecutableData> getExecutables(String contextId) {
    Map<String, ExecutableData> executableList = executables.get(contextId);
    if (executableList == null) {
      executableList = new HashMap<>();
      executables.put(contextId, executableList);
    }
    return executableList;
  }

}
