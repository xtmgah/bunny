package org.rabix.engine.rest.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRepository {

  private final static Logger logger = LoggerFactory.getLogger(JobRepository.class);
  
  private static final String INSERT_JOB = "INSERT INTO JOB (ID,PAYLOAD) VALUES (?,?::json);";
  
  private static final String UPDATE_JOB = "UPDATE JOB SET PAYLOAD=?::json WHERE ID=?;";
  
  private static final String SELECT_JOB = "SELECT PAYLOAD FROM JOB WHERE ID=?;";
  private static final String SELECT_JOBS = "SELECT PAYLOAD FROM JOB;";
  
  public void insert(Job job) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_JOB);
      stmt.setString(1, job.getId());

      PGobject appObject = new PGobject();
      appObject.setType("json");
      appObject.setValue(JSONHelper.writeObject(job));
      stmt.setObject(2, appObject);
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert Job " + job, e);
      throw new DBException("Failed to insert Job " + job, e);
    }
  }
  
  public Job find(String id) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_JOB);
      stmt.setString(1, id);

      ResultSet result = stmt.executeQuery();
      List<Job> apps = convertToJobs(result);
      stmt.close();

      return apps.size() == 1? apps.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find Jobfor id=" + id, e);
      throw new DBException("Failed to find Job for id=" + id, e);
    }
  }
  
  public List<Job> find() throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_JOBS);

      ResultSet result = stmt.executeQuery();
      List<Job> apps = convertToJobs(result);
      stmt.close();

      return apps;
    } catch (SQLException e) {
      logger.error("Failed to find Jobs", e);
      throw new DBException("Failed to find Jobs", e);
    }
  }
  
  public void update(Job job) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(UPDATE_JOB);

      PGobject appObject = new PGobject();
      appObject.setType("json");
      appObject.setValue(JSONHelper.writeObject(job));
      stmt.setObject(1, appObject);
      
      stmt.setString(2, job.getId());
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to update Job " + job, e);
      throw new DBException("Failed to update Job " + job, e);
    }
    
  }
  
  private List<Job> convertToJobs(ResultSet resultSet) throws SQLException {
    List<Job> result = new ArrayList<>();

    while (resultSet.next()) {
      try {
      result.add(JSONHelper.readObject(resultSet.getString("PAYLOAD"), Job.class));
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

}
