package org.rabix.ftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.rabix.common.config.ConfigModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class SimpleFTPServer {

  private int port;
  private String username;
  private String password;
  private String directory;

  @Inject
  public SimpleFTPServer(Configuration configuration) {
    this.port = FTPConfig.getPort(configuration);
    this.username = FTPConfig.getUsername(configuration);
    this.password = FTPConfig.getPassword(configuration);
    this.directory = FTPConfig.getDirectory(configuration);
  }

  public void start() {
    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    UserManager userManager = userManagerFactory.createUserManager();
    BaseUser user = new BaseUser();
    user.setName(username);
    user.setPassword(password);
    user.setHomeDirectory(directory);

    List<Authority> auths = new ArrayList<Authority>();
    Authority auth = new WritePermission();
    auths.add(auth);
    user.setAuthorities(auths);

    try {
      userManager.save(user);

      ListenerFactory listenerFactory = new ListenerFactory();
      listenerFactory.setPort(port);

      FtpServerFactory factory = new FtpServerFactory();
      factory.setUserManager(userManager);
      factory.addListener("default", listenerFactory.createListener());

      FtpServer server = factory.createServer();
      server.start();
    } catch (FtpException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    File configDir = new File("config");
    
    Injector injector = Guice.createInjector(new SimpleFTPModule(), new ConfigModule(configDir, null));
    SimpleFTPServer server = injector.getInstance(SimpleFTPServer.class);
    server.start();
  }

}
