package org.rabix.executor.config;

import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class FileConfig {

  public static final String CALCULATE_FILE_CHECKSUM = "skywalker.calculate_file_checksum";
  public static final String CHECKSUM_ALGORITHM = "skywalker.checksum_algorithm";

  public boolean calculateFileChecksum() {
    return true;
  }

  public HashAlgorithm checksumAlgorithm() {
    return HashAlgorithm.MD5;
  }
}
