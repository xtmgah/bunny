package org.rabix.db.transaction;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransaction {

  private final Connection connection;
  
  public JdbcTransaction(Connection connection) {
    this.connection = connection;
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
