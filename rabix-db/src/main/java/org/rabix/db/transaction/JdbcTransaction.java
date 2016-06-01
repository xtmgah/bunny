package org.rabix.db.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class JdbcTransaction {

  private final Connection connection;
  private final AtomicInteger scopeCounter;
  
  public JdbcTransaction(Connection connection) {
    this.connection = connection;
    this.scopeCounter = new AtomicInteger(0);
  }
  
  public AtomicInteger getScopeCounter() {
    return scopeCounter;
  }
  
  public void increaseScopeCounter() {
    this.scopeCounter.incrementAndGet();
  }

  public void decreaseScopeCounter() {
    this.scopeCounter.decrementAndGet();
  }
  
  public void commit() throws SQLException {
    this.connection.commit();
  }
  
  public void rollback() throws SQLException {
    this.connection.rollback();
  }

  public Connection getConnection() {
    return connection;
  }
  
}
