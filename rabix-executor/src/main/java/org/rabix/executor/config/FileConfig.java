package org.rabix.executor.config;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class FileConfig {

  public static final String BUNNY_CONFORMANCE = "bunny.conformance";

  public static final String SET_FILENAME = "files.set_filename";
  public static final String SET_SIZE = "files.set_size";
  public static final String CALCULATE_FILE_CHECKSUM = "files.calculate_file_checksum";
  public static final String CHECKSUM_ALGORITHM = "files.checksum_algorithm";

  public static final String SECONDARY_FILES_SET_FILENAME = "files.secondary.set_filename";
  public static final String SECONDARY_FILES_SET_SIZE = "files.secondary.set_size";
  public static final String SECONDARY_FILES_CALCULATE_FILE_CHECKSUM = "files.secondary.calculate_file_checksum";
  public static final String SECONDARY_FILES_CHECKSUM_ALGORITHM = "files.secondary.checksum_algorithm";

  public static boolean calculateFileChecksum(Configuration configuration) {
    return configuration.getBoolean(CALCULATE_FILE_CHECKSUM, true);
  }

  public static boolean setFilename(Configuration configuration) {
    if (configuration.getString(BUNNY_CONFORMANCE) != null) {
      return false;
    }
    return configuration.getBoolean(SET_FILENAME, true);
  }

  public static boolean setSize(Configuration configuration) {
    return configuration.getBoolean(SET_SIZE, true);
  }

  public static HashAlgorithm checksumAlgorithm(Configuration configuration) {
    return HashAlgorithm.valueOf(configuration.getString(CHECKSUM_ALGORITHM, "SHA1"));
  }

  public static boolean secondaryFilesCalculateFileChecksum(Configuration configuration) {
    if (configuration.getString(BUNNY_CONFORMANCE) != null) {
      return false;
    }
    return configuration.getBoolean(CALCULATE_FILE_CHECKSUM, true);
  }

  public static boolean secondaryFilesSetFilename(Configuration configuration) {
    if (configuration.getString(BUNNY_CONFORMANCE) != null) {
      return false;
    }
    return configuration.getBoolean(SECONDARY_FILES_SET_FILENAME, true);
  }

  public static boolean secondaryFilesSetSize(Configuration configuration) {
    if (configuration.getString(BUNNY_CONFORMANCE) != null) {
      return false;
    }
    return configuration.getBoolean(SECONDARY_FILES_SET_SIZE);
  }

  public static HashAlgorithm secondaryFilesChecksumAlgorithm(Configuration configuration) {
    return HashAlgorithm.valueOf(configuration.getString(SECONDARY_FILES_CHECKSUM_ALGORITHM, "SHA1"));
  }

}
