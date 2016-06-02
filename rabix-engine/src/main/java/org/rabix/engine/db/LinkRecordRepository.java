package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.rabix.engine.model.LinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;

public class LinkRecordRepository {

  private final static Logger logger = LoggerFactory.getLogger(LinkRecordRepository.class);
  
  private static final String INSERT_LINK_RECORD = "INSERT INTO LINK_RECORD (CONTEXT_ID,SOURCE_JOB_ID,SOURCE_JOB_PORT_ID,SOURCE_TYPE,DESTINATION_JOB_ID,DESTINATION_JOB_PORT_ID,DESTINATION_TYPE,POSITION) VALUES (?,?,?,?,?,?,?,?);";
  private static final String UPDATE_LINK_RECORD = "UPDATE LINK_RECORD SET CONTEXT_ID=?,SOURCE_JOB_ID=?,SOURCE_JOB_PORT_ID=?,SOURCE_TYPE=?,DESTINATION_JOB_ID=?,DESTINATION_JOB_PORT_ID=?,DESTINATION_TYPE=?,POSITION=?) WHERE CONTEXT_ID=? AND SOURCE_JOB_ID=? AND SOURCE_JOB_PORT_ID=? AND SOURCE_TYPE=? AND DESTINATION_JOB_ID=? AND DESTINATION_JOB_PORT_ID=? AND DESTINATION_TYPE=?;";

  private static final String SELECT_LINK_BY_SOURCE = "SELECT * FROM LINK_RECORD WHERE SOURCE_JOB_ID=? AND SOURCE_JOB_PORT_ID=? AND CONTEXT_ID=?;";
  private static final String SELECT_LINK_BY_SOURCE_JOB_ID = "SELECT * FROM LINK_RECORD WHERE SOURCE_JOB_ID=? AND CONTEXT_ID=?;";
  private static final String SELECT_LINK_BY_SOURCE_AND_SOURCE_TYPE = "SELECT * FROM LINK_RECORD WHERE SOURCE_JOB_ID=? AND SOURCE_TYPE=? AND CONTEXT_ID=?;";
  private static final String SELECT_LINK_BY_SOURCE_AND_DESTINATION_TYPE = "SELECT * FROM LINK_RECORD WHERE SOURCE_JOB_ID=? AND SOURCE_JOB_PORT_ID=? AND DESTINATION_TYPE=? AND CONTEXT_ID=?;";
  
  @Transactional
  public void insert(LinkRecord linkRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_LINK_RECORD);

      stmt.setString(1, linkRecord.getContextId());
      stmt.setString(2, linkRecord.getSourceJobId());
      stmt.setString(3, linkRecord.getSourceJobPort());
      stmt.setString(4, linkRecord.getSourceVarType().name());
      stmt.setString(5, linkRecord.getDestinationJobId());
      stmt.setString(6, linkRecord.getDestinationJobPort());
      stmt.setString(7, linkRecord.getDestinationVarType().name());
      
      if (linkRecord.getPosition() == null) {
        stmt.setNull(8, java.sql.Types.INTEGER);
      } else {
        stmt.setInt(8, linkRecord.getPosition());
      }
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert LinkRecord " + linkRecord, e);
      throw new DBException("Failed to insert LinkRecord " + linkRecord, e);
    }
  }
  
  @Transactional
  public void update(LinkRecord linkRecord) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(UPDATE_LINK_RECORD);
      
      stmt.setString(1, linkRecord.getContextId());
      stmt.setString(2, linkRecord.getSourceJobId());
      stmt.setString(3, linkRecord.getSourceJobPort());
      stmt.setString(4, linkRecord.getSourceVarType().name());
      stmt.setString(5, linkRecord.getDestinationJobId());
      stmt.setString(6, linkRecord.getDestinationJobPort());
      stmt.setString(7, linkRecord.getDestinationVarType().name());
      stmt.setInt(8, linkRecord.getPosition());

      stmt.setString(9, linkRecord.getContextId());
      stmt.setString(10, linkRecord.getSourceJobId());
      stmt.setString(11, linkRecord.getSourceJobPort());
      stmt.setString(12, linkRecord.getSourceVarType().name());
      stmt.setString(13, linkRecord.getDestinationJobId());
      stmt.setString(14, linkRecord.getDestinationJobPort());
      stmt.setString(15, linkRecord.getDestinationVarType().name());
      
      stmt.executeUpdate();
      stmt.close();
    } catch (SQLException e) {
      logger.error("Failed to update LinkRecord " + linkRecord, e);
      throw new DBException("Failed to update LinkRecord " + linkRecord, e);
    }
  }
  
  public List<LinkRecord> findBySource(String sourceJobId, String sourceJobPortId, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_LINK_BY_SOURCE);
      stmt.setString(1, sourceJobId);
      stmt.setString(2, sourceJobPortId);
      stmt.setString(3, contextId);

      ResultSet result = stmt.executeQuery();
      List<LinkRecord> linkRecords = convertToLinkRecords(result);
      stmt.close();
      
      return linkRecords;
    } catch (SQLException e) {
      logger.error("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and sourceJobPortId=" + sourceJobPortId + " and contextId=" + contextId, e);
      throw new DBException("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and sourceJobPortId=" + sourceJobPortId + " and contextId=" + contextId, e);
    }
  }
  
  public List<LinkRecord> findBySourceJobId(String sourceJobId, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_LINK_BY_SOURCE_JOB_ID);
      stmt.setString(1, sourceJobId);
      stmt.setString(2, contextId);

      ResultSet result = stmt.executeQuery();
      List<LinkRecord> linkRecords = convertToLinkRecords(result);
      stmt.close();
      
      return linkRecords;
    } catch (SQLException e) {
      logger.error("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and contextId=" + contextId, e);
      throw new DBException("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and contextId=" + contextId, e);
    }
  }
  
  public List<LinkRecord> findBySourceAndSourceType(String sourceJobId, LinkPortType sourceType, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_LINK_BY_SOURCE_AND_SOURCE_TYPE);
      stmt.setString(1, sourceJobId);
      stmt.setString(2, sourceType.name());
      stmt.setString(3, contextId);

      ResultSet result = stmt.executeQuery();
      List<LinkRecord> linkRecords = convertToLinkRecords(result);
      stmt.close();
      
      return linkRecords;
    } catch (SQLException e) {
      logger.error("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and sourceType=" + sourceType + " and contextId=" + contextId, e);
      throw new DBException("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and sourceType=" + sourceType + " and contextId=" + contextId, e);
    }
  }
  
  public List<LinkRecord> findBySourceAndDestinationType(String sourceJobId, LinkPortType destinationType, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_LINK_BY_SOURCE_AND_DESTINATION_TYPE);
      stmt.setString(1, sourceJobId);
      stmt.setString(2, destinationType.name());
      stmt.setString(3, contextId);

      ResultSet result = stmt.executeQuery();
      List<LinkRecord> linkRecords = convertToLinkRecords(result);
      stmt.close();
      
      return linkRecords;
    } catch (SQLException e) {
      logger.error("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and destinationType=" + destinationType + " and contextId=" + contextId, e);
      throw new DBException("Failed to find LinkRecord for sourceJobId=" + sourceJobId + " and destinationType=" + destinationType + " and contextId=" + contextId, e);
    }
  }
  
  private List<LinkRecord> convertToLinkRecords(ResultSet resultSet) throws SQLException {
    List<LinkRecord> result = new ArrayList<>();

    while (resultSet.next()) {
      String contextId = resultSet.getString("CONTEXT_ID");
      String sourceJobId = resultSet.getString("SOURCE_JOB_ID");
      String sourceJobPortId = resultSet.getString("SOURCE_JOB_PORT_ID");
      String sourceType = resultSet.getString("SOURCE_TYPE");
      String destinationJobId = resultSet.getString("DESTINATION_JOB_ID");
      String destinationJobPortId = resultSet.getString("DESTINATION_JOB_PORT_ID");
      String destinationType = resultSet.getString("DESTINATION_TYPE");
      Integer position = resultSet.getInt("POSITION");

      LinkRecord linkRecord = new LinkRecord(contextId, sourceJobId, sourceJobPortId, LinkPortType.valueOf(sourceType), destinationJobId, destinationJobPortId, LinkPortType.valueOf(destinationType), position);
      result.add(linkRecord);
    }
    return result;
  }
  
}
