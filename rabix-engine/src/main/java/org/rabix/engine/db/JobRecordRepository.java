package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.service.JobRecordService.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

public class JobRecordRepository {

  private static final Logger logger = LoggerFactory.getLogger(JobRecordRepository.class);

  private static final String INSERT_JOB_RECORD = "INSERT INTO JOB_RECORD (ID,EXTERNAL_ID,ROOT_ID,PARENT_ID,BLOCKING,JOB_STATE,INPUT_COUNTERS,OUTPUT_COUNTERS,IS_SCATTERED,IS_CONTAINER,IS_SCATTER_WRAPPER,GLOBAL_INPUTS_COUNT,GLOBAL_OUTPUTS_COUNT,SCATTER_STRATEGY) VALUES (?,?,?,?,?,?,?::json,?::json,?,?,?,?,?,?::json);";

  private static final String UPDATE_JOB_RECORD = "UPDATE JOB_RECORD SET ID=?,EXTERNAL_ID=?,ROOT_ID=?,PARENT_ID=?,BLOCKING=?,JOB_STATE=?,INPUT_COUNTERS=?,OUTPUT_COUNTERS=?,IS_SCATTERED=?,IS_CONTAINER=?,IS_SCATTER_WRAPPER=?,GLOBAL_INPUTS_COUNT=?,GLOBAL_OUTPUTS_COUNT=?,SCATTER_STRATEGY=? WHERE ID=? AND ROOT_ID=?";

  private static final String SELECT_JOB_RECORD = "SELECT * FROM JOB_RECORD WHERE ID=? AND ROOT_ID=?;";
  private static final String SELECT_JOB_RECORDS = "SELECT * FROM JOB_RECORD WHERE ROOT_ID=?;";
  private static final String SELECT_ROOT_JOB_RECORD = "SELECT * FROM JOB_RECORD WHERE ID='root' AND ROOT_ID=?;";
  private static final String SELECT_READY_JOB_RECORDS = "SELECT * FROM JOB_RECORD WHERE JOB_STATE='READY' AND ROOT_ID=?;";

  public void insert(JobRecord jobRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_JOB_RECORD);

      stmt.setString(1, jobRecord.getId());
      stmt.setString(2, jobRecord.getExternalId());
      stmt.setString(3, jobRecord.getRootId());
      stmt.setString(4, jobRecord.getParentId());
      stmt.setBoolean(5, jobRecord.isBlocking());
      stmt.setString(6, jobRecord.getState().name());

      PGobject inputCountersObj = new PGobject();
      inputCountersObj.setType("json");
      if (jobRecord.getInputCounters() == null) {
        inputCountersObj.setValue(null);
      } else {
        inputCountersObj.setValue(JSONHelper.writeObject(jobRecord.getInputCounters()));
      }
      stmt.setObject(7, inputCountersObj);

      PGobject outputCountersObj = new PGobject();
      outputCountersObj.setType("json");
      if (jobRecord.getOutputCounters() == null) {
        outputCountersObj.setValue(null);
      } else {
        outputCountersObj.setValue(JSONHelper.writeObject(jobRecord.getOutputCounters()));
      }
      stmt.setObject(8, outputCountersObj);

      stmt.setBoolean(9, jobRecord.isScattered());
      stmt.setBoolean(10, jobRecord.isContainer());
      stmt.setBoolean(11, jobRecord.isScatterWrapper());
      stmt.setInt(12, jobRecord.getGlobalInputsCount());
      stmt.setInt(13, jobRecord.getGlobalOutputsCount());

      PGobject dataObject = new PGobject();
      dataObject.setType("json");
      if (jobRecord.getScatterStrategy() == null) {
        dataObject.setValue(null);
      } else {
        dataObject.setValue(JSONHelper.writeObject(jobRecord.getScatterStrategy()));
      }
      stmt.setObject(14, dataObject);

      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert JobRecord " + jobRecord, e);
      throw new DBException("Failed to insert JobRecord " + jobRecord, e);
    }
  }

  public JobRecord find(String id, String rootId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_JOB_RECORD);
      stmt.setString(1, id);
      stmt.setString(2, rootId);

      ResultSet result = stmt.executeQuery();
      List<JobRecord> jobRecords = convertToJobRecords(result);
      stmt.close();

      return jobRecords.size() == 1? jobRecords.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find JobRecord for id=" + id + " and rootId=" + rootId, e);
      throw new DBException("Failed to find JobRecord for id=" + id + " and rootId=" + rootId, e);
    }
  }
  
  public JobRecord findRoot(String rootId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_ROOT_JOB_RECORD);
      stmt.setString(1, rootId);

      ResultSet result = stmt.executeQuery();
      List<JobRecord> jobRecords = convertToJobRecords(result);
      stmt.close();

      return jobRecords.size() == 1? jobRecords.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find JobRecord for id=" + rootId + " and rootId=" + rootId, e);
      throw new DBException("Failed to find JobRecord for id=" + rootId + " and rootId=" + rootId, e);
    }
  }
  
  public List<JobRecord> findReady(String rootId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_READY_JOB_RECORDS);
      stmt.setString(1, rootId);

      ResultSet result = stmt.executeQuery();
      List<JobRecord> jobRecords = convertToJobRecords(result);
      stmt.close();

      return jobRecords;
    } catch (SQLException e) {
      logger.error("Failed to find JobRecords for rootId=" + rootId, e);
      throw new DBException("Failed to find JobRecords for rootId=" + rootId, e);
    }
  }
  
  public List<JobRecord> find(String rootId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_JOB_RECORDS);
      stmt.setString(1, rootId);

      ResultSet result = stmt.executeQuery();
      List<JobRecord> jobRecords = convertToJobRecords(result);
      stmt.close();

      return jobRecords;
    } catch (SQLException e) {
      logger.error("Failed to find JobRecords for rootId=" + rootId, e);
      throw new DBException("Failed to find JobRecords for rootId=" + rootId, e);
    }
  }

  public JobRecord update(JobRecord jobRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(UPDATE_JOB_RECORD);
      stmt.setString(1, jobRecord.getId());
      stmt.setString(2, jobRecord.getExternalId());
      stmt.setString(3, jobRecord.getRootId());
      stmt.setString(4, jobRecord.getParentId());
      stmt.setBoolean(5, jobRecord.isBlocking());
      stmt.setString(6, jobRecord.getState().name());

      PGobject inputCountersObj = new PGobject();
      inputCountersObj.setType("json");
      inputCountersObj.setValue(JSONHelper.writeObject(jobRecord.getInputCounters()));
      stmt.setObject(7, inputCountersObj);

      PGobject outputCountersObj = new PGobject();
      outputCountersObj.setType("json");
      outputCountersObj.setValue(JSONHelper.writeObject(jobRecord.getOutputCounters()));
      stmt.setObject(8, outputCountersObj);

      stmt.setBoolean(9, jobRecord.isScattered());
      stmt.setBoolean(10, jobRecord.isContainer());
      stmt.setBoolean(11, jobRecord.isScatterWrapper());
      stmt.setInt(12, jobRecord.getGlobalInputsCount());
      stmt.setInt(13, jobRecord.getGlobalOutputsCount());

      PGobject dataObject = new PGobject();
      dataObject.setType("json");
      dataObject.setValue(JSONHelper.writeObject(jobRecord.getScatterStrategy()));
      stmt.setObject(14, dataObject);

      stmt.setString(15, jobRecord.getId());
      stmt.setString(16, jobRecord.getRootId());

      stmt.executeUpdate();
      stmt.close();
      
      return jobRecord;
    } catch (SQLException e) {
      logger.error("Failed to update JobRecord " + jobRecord, e);
      throw new DBException("Failed to update JobRecord " + jobRecord, e);
    }
  }

  private List<JobRecord> convertToJobRecords(ResultSet resultSet) throws SQLException {
    List<JobRecord> result = new ArrayList<>();

    while (resultSet.next()) {
      String id = resultSet.getString("ID");
      String externalId = resultSet.getString("EXTERNAL_ID");
      String rootId = resultSet.getString("ROOT_ID");
      String parentId = resultSet.getString("PARENT_ID");
      Boolean isBlocking = resultSet.getBoolean("BLOCKING");
      String jobState = resultSet.getString("JOB_STATE");
      String inputCounters = resultSet.getString("INPUT_COUNTERS");
      String outputCounters = resultSet.getString("OUTPUT_COUNTERS");
      Boolean isScattered = resultSet.getBoolean("IS_SCATTERED");
      Boolean isContainer = resultSet.getBoolean("IS_CONTAINER");
      Boolean isScatterWrapper = resultSet.getBoolean("IS_SCATTER_WRAPPER");
      Integer globalInputsCount = resultSet.getInt("GLOBAL_INPUTS_COUNT");
      Integer globalOutputsCount = resultSet.getInt("GLOBAL_OUTPUTS_COUNT");
      String scatterStrategy = resultSet.getString("SCATTER_STRATEGY");

      JobRecord jobRecord = new JobRecord(rootId, id, externalId, parentId, JobState.valueOf(jobState), isContainer, isScattered, isBlocking);
      jobRecord.setScatterWrapper(isScatterWrapper);
      jobRecord.setGlobalInputsCount(globalInputsCount);
      jobRecord.setGlobalOutputsCount(globalOutputsCount);
      jobRecord.setScatterStrategy(JSONHelper.readObject(scatterStrategy, ScatterStrategy.class));
      jobRecord.setInputCounters(JSONHelper.readObject(inputCounters, new TypeReference<List<PortCounter>>() {}));
      jobRecord.setOutputCounters(JSONHelper.readObject(outputCounters, new TypeReference<List<PortCounter>>() {}));
      result.add(jobRecord);
    }
    return result;
  }
  
}
