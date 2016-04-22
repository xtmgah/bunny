package org.rabix.bindings.protocol.draft3.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.protocol.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.protocol.draft3.service.Draft3GlobException;
import org.rabix.bindings.protocol.draft3.service.Draft3GlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class Draft3GlobServiceImpl implements Draft3GlobService {

  private final Logger logger = LoggerFactory.getLogger(Draft3GlobServiceImpl.class);

  /**
   * Find all files that match GLOB inside the working directory 
   */
  public Set<File> glob(Draft3Job job, File workingDir, Object glob) throws Draft3GlobException {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(workingDir);
    
    try {
      glob = Draft3ExpressionResolver.evaluate(glob, job, null);
    } catch (Draft3ExpressionException e) {
      logger.error("Failed to evaluate glob " + glob, e);
      throw new Draft3GlobException("Failed to evaluate glob " + glob, e);
    }
    if (glob == null || !(glob instanceof String)) {
      return Collections.<File> emptySet();
    }
    
    final Set<File> files = new HashSet<>();
    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    try {
      Files.walkFileTree(workingDir.toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (matcher.matches(file.getFileName())) {
            files.add(file.toFile());
          }
          return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      logger.error("Failed to traverse through working directory", e);
      throw new Draft3GlobException("Failed to traverse through working directory", e);
    }
    return files;
  }

}
