package org.rabix.bindings.protocol.draft2.service.impl;

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

import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.protocol.draft2.expression.helper.Draft2ExpressionBeanHelper;
import org.rabix.bindings.protocol.draft2.service.Draft2GlobException;
import org.rabix.bindings.protocol.draft2.service.Draft2GlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class Draft2GlobServiceImpl implements Draft2GlobService {

  private final Logger logger = LoggerFactory.getLogger(Draft2GlobServiceImpl.class);

  /**
   * Find all files that match GLOB inside the working directory 
   */
  public Set<File> glob(Draft2Job job, File workingDir, Object glob) throws Draft2GlobException {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(workingDir);
    
    if (Draft2ExpressionBeanHelper.isExpression(glob)) {
      try {
        glob = Draft2ExpressionBeanHelper.<String> evaluate(job, glob);
      } catch (Draft2ExpressionException e) {
        logger.error("Failed to evaluate glob " + glob, e);
        throw new Draft2GlobException("Failed to evaluate glob " + glob, e);
      }
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
      throw new Draft2GlobException("Failed to traverse through working directory", e);
    }
    return files;
  }

}
