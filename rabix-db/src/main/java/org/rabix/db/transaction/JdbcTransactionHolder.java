package org.rabix.db.transaction;

public class JdbcTransactionHolder {

  private static ThreadLocal<JdbcTransaction> currentTransaction = new ThreadLocal<JdbcTransaction>();

  public static void setCurrentTransaction(JdbcTransaction transaction) {
    currentTransaction.set(transaction);
  }

  public static JdbcTransaction getCurrentTransaction() {
    return currentTransaction.get();
  }

  public static void removeCurrentTransaction() {
    currentTransaction.remove();
  }
}
