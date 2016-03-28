package org.rabix.bindings.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIHelper {

  private final static Logger logger = LoggerFactory.getLogger(URIHelper.class);

  private static final String DEFAULT_ENCODING = "UTF-8";

  public final static String HTTP_URI_SCHEME = "http";
  public final static String DATA_URI_SCHEME = "data";
  public final static String FILE_URI_SCHEME = "file";
  
  private final static String DATA_URI_BASE_64 = "base64";
  private final static String DATA_URI_PAYLOAD_SEPARATOR = ",";
  
  public static String getData(String uri) throws IOException {
    if (isHTTP(uri)) {
      return fetchFromHTTP(uri);
    }
    if (isFile(uri)) {
      return loadFromFile(uri);
    }
    if (isData(uri)) {
      return loadData(uri);
    }
    return null;
  }
  
  public static String createURI(String scheme, String payload) {
    return scheme + ":" + payload;
  }
  
  public static boolean isFile(String uri) {
    return uri.startsWith(FILE_URI_SCHEME);
  }
  
  public static boolean isData(String uri) {
    return uri.startsWith(DATA_URI_SCHEME);
  }
  
  public static boolean isHTTP(String uri) {
    return uri.startsWith(HTTP_URI_SCHEME);
  }
  
  public static String getURIInfo(String uri) {
    if (isFile(uri)) {
      return uri.substring(FILE_URI_SCHEME.length() + 1);
    }
    if (isData(uri)) {
      return uri.substring(DATA_URI_PAYLOAD_SEPARATOR.length() + 1);
    }
    if (isHTTP(uri)) {
      return uri.substring(HTTP_URI_SCHEME.length() + 1);
    }
    return null;
  }

  private static String fetchFromHTTP(String uri) throws IOException {
    try {
      URL website = new URL(uri);

      URLConnection connection = website.openConnection();
      BufferedReader in = null;
      try {
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        return response.toString();
      } finally {
        if (in != null) {
          in.close();
        }
      }
    } catch (IOException e) {
      logger.error("Failed to load data from URL " + uri, e);
      throw e;
    }
  }

  private static String loadFromFile(String uri) throws IOException {
    try {
      String filePath = getURIInfo(uri);
      String absoluteFilePath = new File(filePath).getCanonicalPath();
      return FileUtils.readFileToString(new File(absoluteFilePath), DEFAULT_ENCODING);
    } catch (IOException e) {
      logger.error("Failed to load data from file " + uri, e);
      throw e;
    }
  }

  private static String loadData(String uri) throws IOException {
    String data = getURIInfo(uri);
    String headers = data.substring(0, data.indexOf(DATA_URI_PAYLOAD_SEPARATOR));
    String payload = data.substring(data.indexOf(DATA_URI_PAYLOAD_SEPARATOR) + 1);

    boolean isBase64Encoded = headers.contains(DATA_URI_BASE_64);
    if (isBase64Encoded) {
      try {
        return new String(Base64.decodeBase64(payload), DEFAULT_ENCODING);
      } catch (UnsupportedEncodingException e) {
        logger.error("Failed to decode payload from URI " + uri);
        throw new IOException(e);
      }
    }
    return payload;
  }
  
}
