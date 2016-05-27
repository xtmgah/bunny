package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.model.scatter.impl.ScatterZipStrategy;
import org.rabix.engine.service.JobRecordService.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRecordDAO {

  private static final Logger logger = LoggerFactory.getLogger(JobRecordDAO.class);

  private static final String INSERT_JOB_RECORD = "INSERT INTO JOB_RECORD (ID,EXTERNAL_ID,ROOT_ID,PARENT_ID,BLOCKING,JOB_STATE,INPUT_COUNTERS,OUTPUT_COUNTERS,IS_SCATTERED,IS_CONTAINER,IS_SCATTER_WRAPPER,GLOBAL_INPUTS_COUNT,GLOBAL_OUTPUTS_COUNT,SCATTER_STRATEGY) " + "VALUES (?,?,?,?,?,?,?::json,?::json,?,?,?,?,?,?::json);";
  private static final String UPDATE_JOB_RECORD = "UPDATE JOB_RECORD SET ID=?,EXTERNAL_ID=?,ROOT_ID=?,PARENT_ID=?,BLOCKING=?,JOB_STATE=?,INPUT_COUNTERS=?,OUTPUT_COUNTERS=?,IS_SCATTERED=?,IS_CONTAINER=?,IS_SCATTER_WRAPPER=?,GLOBAL_INPUTS_COUNT=?,GLOBAL_OUTPUTS_COUNT=?,SCATTER_STRATEGY=? WHERE ID=? AND ROOT_ID=?";

//  public static void main(String[] args) throws DBException {
//    JobRecord jobRecord = new JobRecord("rootId", "id2", UUID.randomUUID().toString(), "parentId", JobState.READY, false, false, true);
//
//    List<PortCounter> inputCounters = new ArrayList<>();
//    inputCounters.add(new PortCounter("i", 1, true));
//    jobRecord.setInputCounters(inputCounters);
//
//    List<PortCounter> outputCounters = new ArrayList<>();
//    outputCounters.add(new PortCounter("o", 1, false));
//    jobRecord.setOutputCounters(outputCounters);
//
//    ScatterStrategy scatterStrategy = new ScatterZipStrategy();
//    jobRecord.setScatterStrategy(scatterStrategy);
//    jobRecord.setScatterWrapper(true);
//
//    insert(jobRecord);
//    jobRecord.setContainer(true);
//    update(jobRecord);
//  }

  public static Connection connect() {
    try {
      Class.forName("org.postgresql.Driver");
      Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bunnydb", "postgres", "admin");
      connection.setAutoCommit(false);
      return connection;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void insert(JobRecord jobRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = connect();

      stmt = c.prepareStatement(INSERT_JOB_RECORD);
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

      stmt.executeUpdate();
      stmt.close();
      c.commit();
      c.close();
    } catch (SQLException e) {
      logger.error("Failed to insert JobRecord " + jobRecord, e);
      throw new DBException("Failed to insert JobRecord " + jobRecord, e);
    }
  }
  
  public static void update(JobRecord jobRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = connect();

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
      c.commit();
      c.close();
    } catch (SQLException e) {
      logger.error("Failed to insert JobRecord " + jobRecord, e);
      throw new DBException("Failed to insert JobRecord " + jobRecord, e);
    }
  }

}
