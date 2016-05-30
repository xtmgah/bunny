package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.util.PGobject;
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
}
