package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextRecordRepository {

  private final static Logger logger = LoggerFactory.getLogger(ContextRecordRepository.class);
  
  private static final String INSERT_CONTEXT_RECORD = "INSERT INTO CONTEXT_RECORD (ID,STATUS,CONFIG) VALUES (?,?,?::json);";
  private static final String UPDATE_CONTEXT_RECORD = "UPDATE CONTEXT_RECORD SET STATUS=?,CONFIG=? WHERE ID=?;";
  private static final String SELECT_CONTEXT_RECORD = "SELECT * FROM CONTEXT_RECORD WHERE ID=?;";
  
  public void insert(ContextRecord contextRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_CONTEXT_RECORD);
      stmt.setString(1, contextRecord.getId());
      stmt.setString(2, contextRecord.getStatus().name());

      PGobject configObject = new PGobject();
      configObject.setType("json");
      configObject.setValue(JSONHelper.writeObject(contextRecord.getConfig()));
      stmt.setObject(3, configObject);
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert ContextRecord " + contextRecord, e);
      throw new DBException("Failed to insert ContextRecord " + contextRecord, e);
    }
  }
  
  public void update(ContextRecord contextRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(UPDATE_CONTEXT_RECORD);
      stmt.setString(1, contextRecord.getStatus().name());
      
      PGobject configObject = new PGobject();
      configObject.setType("json");
      configObject.setValue(JSONHelper.writeObject(contextRecord.getConfig()));
      stmt.setObject(2, configObject);
      
      stmt.setString(3, contextRecord.getId());
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to update ContextRecord " + contextRecord, e);
      throw new DBException("Failed to update ContextRecord " + contextRecord, e);
    }
  }
  
  public ContextRecord find(String id) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_CONTEXT_RECORD);
      stmt.setString(1, id);

      ResultSet result = stmt.executeQuery();
      List<ContextRecord> contextRecords = convertToContextRecords(result);
      stmt.close();

      return contextRecords.size() == 1? contextRecords.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find ContextRecord for id=" + id, e);
      throw new DBException("Failed to find ContextRecord for id=" + id, e);
    }
  }
  
  private List<ContextRecord> convertToContextRecords(ResultSet resultSet) throws SQLException {
    List<ContextRecord> result = new ArrayList<>();

    while (resultSet.next()) {
      String id = resultSet.getString("ID");
      String config = resultSet.getString("CONFIG");
      String status = resultSet.getString("STATUS");

      Map<String, Object> configObject = JSONHelper.readMap(config);
      result.add(new ContextRecord(id, configObject, ContextStatus.valueOf(status)));
    }
    return result;
  }
  
}
