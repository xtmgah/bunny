package org.rabix.db;

import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.rabix.db.transaction.JdbcTransactionInterceptor;
import org.rabix.db.transaction.JdbcTransactionService;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.Transactional;

public class DBModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataSource.class).toProvider(PostgreSQLProvider.class).in(Scopes.SINGLETON);
    
    bind(JdbcTransactionService.class).in(Scopes.SINGLETON);

    JdbcTransactionInterceptor jdbcTransactionInterceptor = new JdbcTransactionInterceptor();
    requestInjection(jdbcTransactionInterceptor);
    
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), jdbcTransactionInterceptor);
  }

  static class PostgreSQLProvider implements Provider<DataSource> {
    
    private final String dbHost;
    private final String dbName;
    private final String username;
    private final String password;
    
    private final int dbConnectionPoolSize;
    
    @Inject
    public PostgreSQLProvider(Configuration configuration) {
      this.dbHost = configuration.getString("db.host");
      this.dbName = configuration.getString("db.dbname");
      this.username = configuration.getString("db.username");
      this.password = configuration.getString("db.password");
      this.dbConnectionPoolSize = configuration.getInt("db.connection.pool.size");
    }
    
    @Override
    public DataSource get() {
      BasicDataSource datasource = new BasicDataSource();

      datasource.setUsername(username);
      datasource.setPassword(password);
      datasource.setDriverClassName("org.postgresql.Driver");
      datasource.setUrl(getUrl());
      datasource.setInitialSize(dbConnectionPoolSize);
      datasource.setDefaultAutoCommit(false);
      return datasource;
    }
    
    private String getUrl() {
      return dbHost.endsWith("/") ? dbHost + dbName : dbHost + "/" + dbName;
    }
    
  }
  
}
