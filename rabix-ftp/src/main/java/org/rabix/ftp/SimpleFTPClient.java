package org.rabix.ftp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SimpleFTPClient {

  private final static Logger logger = LoggerFactory.getLogger(SimpleFTPClient.class);
  
  private int port;
  private String host;
  private String username;
  private String password;

  @Inject
  public SimpleFTPClient(Configuration configuration) {
    this.port = FTPConfig.getPort(configuration);
    this.host = FTPConfig.getHost(configuration);
    this.username = FTPConfig.getUsername(configuration);
    this.password = FTPConfig.getPassword(configuration);
  }

  public void download(File workingDir, String remotePath) throws IOException {
    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(host, port);
      ftpClient.login(username, password);
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      File localWorkingDir = workingDir;
      String[] parts = remotePath.split(File.separator);
      for (int i = 0; i < parts.length - 1; i++) {
        if (parts[i].isEmpty()) {
          continue;
        }
        String remoteWorkingDir = parts[i];
        localWorkingDir = new File(localWorkingDir, remoteWorkingDir);
        if (!localWorkingDir.exists()) {
          localWorkingDir.mkdirs();
        }
      }
      File file = new File(localWorkingDir, parts[parts.length - 1]);
      OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
      boolean success = ftpClient.retrieveFile(remotePath, os);
      os.close();

      if (success) {
        logger.debug("File {} has been downloaded successfully.", remotePath);
      }
    } catch (IOException e) {
      throw e;
    } finally {
      try {
        if (ftpClient.isConnected()) {
          ftpClient.logout();
          ftpClient.disconnect();
        }
      } catch (IOException ex) {
        // do nothing
      }
    }
  }

  public void upload(File file, String remotePath) throws IOException {
    FTPClient ftp = new FTPClient();
    int reply;
    try {
      ftp.connect(host, port);

      reply = ftp.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        ftp.disconnect();
        throw new IOException("Exception in connecting to FTP Server");
      }
      ftp.login(username, password);
      ftp.setFileType(FTP.BINARY_FILE_TYPE);
      ftp.enterLocalPassiveMode();

      String[] paths = remotePath.split(File.separator);
      
      for(int i = 0; i < paths.length - 1; i++) {
        if (paths[i].isEmpty()) {
          continue;
        }
        boolean exists = changeWorkingDirectory(ftp, paths[i]);
        if (!exists) {
          ftp.makeDirectory(paths[i]);
          changeWorkingDirectory(ftp, paths[i]);
        }
      }
      try (InputStream input = new FileInputStream(new File(file.getAbsolutePath()))) {
        ftp.storeFile(paths[paths.length - 1], input);
      }
      ftp.disconnect();
    } catch (IOException e) {
      throw e;
    }
  }
  
  private static boolean changeWorkingDirectory(FTPClient ftpClient, String dirPath) throws IOException {
    ftpClient.changeWorkingDirectory(dirPath);
    int returnCode = ftpClient.getReplyCode();
    if (returnCode == 550) {
      return false;
    }
    return true;
  }
  
}
