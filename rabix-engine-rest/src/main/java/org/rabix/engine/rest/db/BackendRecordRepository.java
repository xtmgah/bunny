package org.rabix.engine.rest.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.rabix.transport.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;

public class BackendRecordRepository {

  private final static Logger logger = LoggerFactory.getLogger(BackendRecordRepository.class);

  private static final String INSERT_BACKEND_RECORD = "INSERT INTO BACKEND_RECORD (ID,HEARTBEAT,ACTIVE,BACKEND) VALUES (?,?,?,?::json);";
  
  private static final String UPDATE_BACKEND_RECORD = "UPDATE BACKEND_RECORD SET HEARTBEAT=?,ACTIVE=?,BACKEND=? WHERE ID=?;";
  private static final String UPDATE_BACKEND_RECORD_HEARTBEAT = "UPDATE BACKEND_RECORD SET HEARTBEAT=? WHERE ID=?;";
  
  private static final String SELECT_BACKEND_RECORD = "SELECT * FROM BACKEND_RECORD WHERE ID=?;";
  private static final String SELECT_ACTIVE_BACKEND_RECORDS = "SELECT * FROM BACKEND_RECORD WHERE ACTIVE='true';";

  @Transactional
  public void insert(Backend backend, boolean isActive) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_BACKEND_RECORD);
      stmt.setString(1, backend.getId());

      long milis = new Date().getTime();
      stmt.setTimestamp(2, new Timestamp(milis));

      stmt.setBoolean(3, isActive);

      PGobject appObject = new PGobject();
      appObject.setType("json");
      appObject.setValue(JSONHelper.writeObject(backend));
      stmt.setObject(4, appObject);

      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert BackendRecord " + backend, e);
      throw new DBException("Failed to insert BackendRecord " + backend, e);
    }
  }

  @Transactional
  public void update(BackendRecord backendRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(UPDATE_BACKEND_RECORD);
      stmt.setTimestamp(1, new Timestamp(backendRecord.getHeartbeatTime()));
      stmt.setBoolean(2, backendRecord.isActive());

      PGobject appObject = new PGobject();
      appObject.setType("json");
      appObject.setValue(JSONHelper.writeObject(backendRecord.getBackend()));
      stmt.setObject(3, appObject);

      stmt.setString(4, backendRecord.getId());
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to update BackendRecord " + backendRecord, e);
      throw new DBException("Failed to update BackendRecord " + backendRecord, e);
    }
  }
  
  @Transactional
  public void updateHeartbeat(String id, Long heartbeatTime) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(UPDATE_BACKEND_RECORD_HEARTBEAT);
      stmt.setTimestamp(1, new Timestamp(heartbeatTime));
      
      stmt.setString(2, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to update heartbeat for " + id, e);
      throw new DBException("Failed to update heartbeat for " + id, e);
    }
  }
  
  @Transactional
  public BackendRecord find(String id) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_BACKEND_RECORD);
      stmt.setString(1, id);

      ResultSet result = stmt.executeQuery();
      List<BackendRecord> backends = convertToBackendRecords(result);
      stmt.close();

      return backends.size() == 1 ? backends.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find BackendRecord for id=" + id, e);
      throw new DBException("Failed to find BackendRecord for id=" + id, e);
    }
  }

  @Transactional
  public List<BackendRecord> findActive() throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_ACTIVE_BACKEND_RECORDS);

      ResultSet result = stmt.executeQuery();
      List<BackendRecord> backends = convertToBackendRecords(result);
      stmt.close();
      return backends;
    } catch (SQLException e) {
      logger.error("Failed to find active BackendRecords", e);
      throw new DBException("Failed to find active BackendRecords", e);
    }
  }

  private List<BackendRecord> convertToBackendRecords(ResultSet resultSet) throws SQLException {
    List<BackendRecord> result = new ArrayList<>();

    while (resultSet.next()) {
      try {
        String id = resultSet.getString("ID");
        boolean isActive = resultSet.getBoolean("ACTIVE");
        String backend = resultSet.getString("BACKEND");
        Timestamp heartbeat = resultSet.getTimestamp("HEARTBEAT");

        Backend backendObj = JSONHelper.readObject(backend, Backend.class);

        result.add(new BackendRecord(id, heartbeat.getTime(), isActive, backendObj));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

}
