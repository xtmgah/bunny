package org.rabix.db.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JdbcTransactionService {

  private static final Logger logger = LoggerFactory.getLogger(JdbcTransactionService.class);

  private DataSource dataSource;

  @Inject
  public JdbcTransactionService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void begin() throws SQLException {
    logger.debug("Start the transaction");

    JdbcTransaction transaction = JdbcTransactionHolder.getCurrentTransaction();

    Connection connection = null;
    if (transaction == null) {
      connection = dataSource.getConnection();
    } else {
      connection = transaction.getConnection();
    }
    
    logger.debug("Save the transaction for thread: {}.", Thread.currentThread());
    JdbcTransactionHolder.setCurrentTransaction(new JdbcTransaction(connection));
  }

  public void commit() throws SQLException {
    logger.debug("Commit the transaction");

    try {
      logger.debug("Get the connection for thread: {}.", Thread.currentThread());
      JdbcTransaction transaction = JdbcTransactionHolder.getCurrentTransaction();
      transaction.commit();
      transaction.getConnection().close();
    } finally {
      JdbcTransactionHolder.removeCurrentTransaction();
    }
  }

  public void rollback() throws SQLException {
    logger.debug("Rollback the transaction");

    try {
      logger.debug("Get the transaction for thread: {}.", Thread.currentThread());
      JdbcTransaction transaction = JdbcTransactionHolder.getCurrentTransaction();
      transaction.rollback();
      transaction.getConnection().close();
    } finally {
      JdbcTransactionHolder.removeCurrentTransaction();
    }

  }
}
