package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
      stmt.setInt(8, linkRecord.getPosition());
      
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
}
