package org.rabix.executor.config;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class FileConfig {

  public static final String RABIX_CONFORMANCE = "rabix.conformance";
  
  public static final String CALCULATE_FILE_CHECKSUM = "rabix.calculate_file_checksum";
  public static final String CHECKSUM_ALGORITHM = "rabix.checksum_algorithm";

  public static boolean calculateFileChecksum(Configuration configuration) {
    return configuration.getBoolean(CALCULATE_FILE_CHECKSUM, true);
  }

  public static HashAlgorithm checksumAlgorithm(Configuration configuration) {
    return HashAlgorithm.valueOf(configuration.getString(CHECKSUM_ALGORITHM, "SHA1"));
  }

}
