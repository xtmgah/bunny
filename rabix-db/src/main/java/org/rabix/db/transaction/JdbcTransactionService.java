package org.rabix.db.transaction;

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
    JdbcTransaction transaction = JdbcTransactionHolder.getCurrentTransaction();

    if (transaction == null) {
      transaction = new JdbcTransaction(dataSource.getConnection());
      JdbcTransactionHolder.setCurrentTransaction(transaction);
    }
    
    transaction.increaseScopeCounter();
  }

  public void commit() throws SQLException {
    JdbcTransaction transaction = JdbcTransactionHolder.getCurrentTransaction();
    transaction.decreaseScopeCounter();
    
    if (transaction.getScopeCounter().get() > 0) {
      return;
    }
    logger.debug("Commit the transaction");
    try {
      transaction.commit();
      transaction.getConnection().close();
    } finally {
      JdbcTransactionHolder.removeCurrentTransaction();
    }
  }

  public void rollback() throws SQLException {
    logger.debug("Rollback the transaction");
    try {
      JdbcTransaction transaction = JdbcTransactionHolder.getCurrentTransaction();
      transaction.rollback();
      transaction.getConnection().close();
    } finally {
      JdbcTransactionHolder.removeCurrentTransaction();
    }
  }
}
