package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.rabix.engine.model.VariableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;

public class VariableRecordRepository {

  private final static Logger logger = LoggerFactory.getLogger(VariableRecordRepository.class);
  
  private static final String INSERT_VARIABLE_RECORD = "INSERT INTO VARIABLE_RECORD (JOB_ID,VALUE,PORT_ID,TYPE,LINK_MERGE,IS_WRAPPED,GLOBALS_COUNT,TIMES_UPDATED_COUNT,CONTEXT_ID,IS_DEFAULT) VALUES (?,?::json,?,?,?,?,?,?,?,?);";
  
  private static final String UPDATE_VARIABLE_RECORD = "UPDATE VARIABLE_RECORD SET JOB_ID=?,VALUE=?,PORT_ID=?,TYPE=?,LINK_MERGE=?,IS_WRAPPED=?,GLOBALS_COUNT=?,TIMES_UPDATED_COUNT=?,CONTEXT_ID=?,IS_DEFAULT=? WHERE PORT_ID=? AND CONTEXT_ID=? AND JOB_ID=? AND TYPE=?";
  
  private static final String SELECT_VARIABLE_RECORD = "SELECT * FROM VARIABLE_RECORD WHERE JOB_ID=? AND PORT_ID=? AND TYPE=? AND CONTEXT_ID=?;";
  private static final String SELECT_VARIABLE_RECORDS_BY_TYPE = "SELECT * FROM VARIABLE_RECORD WHERE JOB_ID=? AND TYPE=? AND CONTEXT_ID=?;";
  private static final String SELECT_VARIABLE_RECORDS_BY_PORT_ID = "SELECT * FROM VARIABLE_RECORD WHERE JOB_ID=? AND PORT_ID=? AND CONTEXT_ID=?;";
  
  @Transactional
  public void insert(VariableRecord variableRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_VARIABLE_RECORD);

      stmt.setString(1, variableRecord.getJobId());
      
      PGobject valueObj = new PGobject();
      valueObj.setType("json");
      valueObj.setValue(JSONHelper.writeObject(variableRecord.getValue()));
      stmt.setObject(2, valueObj);
      
      stmt.setString(3, variableRecord.getPortId());
      stmt.setString(4, variableRecord.getType().name());
      stmt.setString(5, variableRecord.getLinkMerge().name());
      stmt.setBoolean(6, variableRecord.isWrapped());
      stmt.setInt(7, variableRecord.getGlobalsCount());
      stmt.setInt(8, variableRecord.getTimesUpdatedCount());
      stmt.setString(9, variableRecord.getContextId());
      stmt.setBoolean(10, variableRecord.isDefault());
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert VariableRecord " + variableRecord, e);
      throw new DBException("Failed to insert VariableRecord " + variableRecord, e);
    }
  }
  
  @Transactional
  public void update(VariableRecord variableRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(UPDATE_VARIABLE_RECORD);
      
      stmt.setString(1, variableRecord.getJobId());
      
      PGobject valueObj = new PGobject();
      valueObj.setType("json");
      valueObj.setValue(JSONHelper.writeObject(variableRecord.getValue()));
      stmt.setObject(2, valueObj);
      
      stmt.setString(3, variableRecord.getPortId());
      stmt.setString(4, variableRecord.getType().name());
      stmt.setString(5, variableRecord.getLinkMerge().name());
      stmt.setBoolean(6, variableRecord.isWrapped());
      stmt.setInt(7, variableRecord.getGlobalsCount());
      stmt.setInt(8, variableRecord.getTimesUpdatedCount());
      stmt.setString(9, variableRecord.getContextId());
      stmt.setBoolean(10, variableRecord.isDefault());

      stmt.setString(11, variableRecord.getPortId());
      stmt.setString(12, variableRecord.getContextId());
      stmt.setString(13, variableRecord.getJobId());
      stmt.setString(14, variableRecord.getType().name());
      
      stmt.executeUpdate();
      stmt.close();
    } catch (SQLException e) {
      logger.error("Failed to update VariableRecord " + variableRecord, e);
      throw new DBException("Failed to update VariableRecord " + variableRecord, e);
    }
  }
  
  public VariableRecord find(String jobId, String portId, LinkPortType type, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_VARIABLE_RECORD);
      stmt.setString(1, jobId);
      stmt.setString(2, portId);
      stmt.setString(3, type.name());
      stmt.setString(4, contextId);

      ResultSet result = stmt.executeQuery();
      List<VariableRecord> variableRecords = convertToVariableRecords(result);
      stmt.close();

      return variableRecords.size() == 1? variableRecords.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find VariableRecord for jobId=" + jobId + " and portId=" + portId + " and type=" + type + " and contextId=" + contextId, e);
      throw new DBException("Failed to find VariableRecord for jobId=" + jobId + " and portId=" + portId + " and type=" + type + " and contextId=" + contextId, e);
    }
  }
  
  public List<VariableRecord> findByType(String jobId, LinkPortType type, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_VARIABLE_RECORDS_BY_TYPE);
      stmt.setString(1, jobId);
      stmt.setString(2, type.name());
      stmt.setString(3, contextId);

      ResultSet result = stmt.executeQuery();
      List<VariableRecord> variableRecords = convertToVariableRecords(result);
      stmt.close();

      return variableRecords;
    } catch (SQLException e) {
      logger.error("Failed to find VariableRecords for jobId=" + jobId + " and type=" + type + " and contextId=" + contextId, e);
      throw new DBException("Failed to find VariableRecords for jobId=" + jobId + " and type=" + type + " and contextId=" + contextId, e);
    }
  }
  
  public List<VariableRecord> findByPort(String jobId, String portId, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_VARIABLE_RECORDS_BY_PORT_ID);
      stmt.setString(1, jobId);
      stmt.setString(2, portId);
      stmt.setString(3, contextId);

      ResultSet result = stmt.executeQuery();
      List<VariableRecord> variableRecords = convertToVariableRecords(result);
      stmt.close();

      return variableRecords;
    } catch (SQLException e) {
      logger.error("Failed to find VariableRecord for jobId=" + jobId + " and portId=" + portId + " and contextId=" + contextId, e);
      throw new DBException("Failed to find VariableRecord for jobId=" + jobId + " and portId=" + portId + " and contextId=" + contextId, e);
    }
  }
  
  private List<VariableRecord> convertToVariableRecords(ResultSet resultSet) throws SQLException {
    List<VariableRecord> result = new ArrayList<>();

    while (resultSet.next()) {
      String jobId = resultSet.getString("JOB_ID");
      String value = resultSet.getString("VALUE");
      String portId = resultSet.getString("PORT_ID");
      String type = resultSet.getString("TYPE");
      String linkMerge = resultSet.getString("LINK_MERGE");
      Boolean isWrapped = resultSet.getBoolean("IS_WRAPPED");
      Integer globalsCount = resultSet.getInt("GLOBALS_COUNT");
      Integer timesUpdatedCount = resultSet.getInt("TIMES_UPDATED_COUNT");
      String contextId = resultSet.getString("CONTEXT_ID");
      Boolean isDefault = resultSet.getBoolean("IS_DEFAULT");

      Object valueObject = JSONHelper.transform(JSONHelper.readJsonNode(value));
      
      VariableRecord variableRecord = new VariableRecord(contextId, jobId, portId, LinkPortType.valueOf(type), valueObject, LinkMerge.valueOf(linkMerge));
      variableRecord.setWrapped(isWrapped);
      variableRecord.setGlobalsCount(globalsCount);
      variableRecord.setTimesUpdatedCount(timesUpdatedCount);
      variableRecord.setDefault(isDefault);
      result.add(variableRecord);
    }
    return result;
  }
  
}
