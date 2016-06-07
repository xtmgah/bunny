package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationRepository {

private final static Logger logger = LoggerFactory.getLogger(DAGNodeRepository.class);
  
  private static final String INSERT_APPLICATION = "INSERT INTO APPLICATION (ID,PAYLOAD) SELECT ?,?::json WHERE NOT EXISTS (SELECT ID FROM APPLICATION WHERE ID=?);";
  private static final String SELECT_APPLICATION = "SELECT PAYLOAD FROM APPLICATION WHERE ID=?;";
  
  public void insert(String id, String payload) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_APPLICATION);
      stmt.setString(1, id);

      PGobject appObject = new PGobject();
      appObject.setType("json");
      appObject.setValue(payload);
      stmt.setObject(2, appObject);
      
      stmt.setString(3, id);
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert Application " + payload, e);
      throw new DBException("Failed to insert Application " + payload, e);
    }
  }
  
  public String find(String id) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_APPLICATION);
      stmt.setString(1, id);

      ResultSet result = stmt.executeQuery();
      List<String> apps = convertToAppPayloads(result);
      stmt.close();

      return apps.size() == 1? apps.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find Application payload for id=" + id, e);
      throw new DBException("Failed to find Application payload for id=" + id, e);
    }
  }
  
  private List<String> convertToAppPayloads(ResultSet resultSet) throws SQLException {
    List<String> result = new ArrayList<>();

    while (resultSet.next()) {
      try {
      String app = resultSet.getString("PAYLOAD");
      result.add(app);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }
  
}
