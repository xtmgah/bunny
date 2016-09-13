package org.rabix.bindings.cwl1.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionException;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionResolver;
import org.rabix.bindings.cwl1.service.CWL1GlobException;
import org.rabix.bindings.cwl1.service.CWL1GlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class CWL1GlobServiceImpl implements CWL1GlobService {

  private final Logger logger = LoggerFactory.getLogger(CWL1GlobServiceImpl.class);

  /**
   * Find all files that match GLOB inside the working directory 
   */
  @SuppressWarnings("unchecked")
  public Set<File> glob(CWL1Job job, File workingDir, Object glob) throws CWL1GlobException {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(workingDir);
    
    try {
      if (CWL1ExpressionResolver.isExpressionObject(glob)) {
        glob = CWL1ExpressionResolver.resolve(glob, job, null);
      }
    } catch (CWL1ExpressionException e) {
      logger.error("Failed to evaluate glob " + glob, e);
      throw new CWL1GlobException("Failed to evaluate glob " + glob, e);
    }
    if (glob == null) {
      return Collections.<File> emptySet();
    }
    List<String> globs = new ArrayList<>();
    if (glob instanceof List<?>) {
      globs = (List<String>) glob;
    } else {
      globs.add((String) glob);
    }
    
    final Set<File> files = new HashSet<>();
    for (String singleGlob : globs) {
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + singleGlob);
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
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (matcher.matches(dir.getFileName())) {
              files.add(dir.toFile());
            }
            return super.preVisitDirectory(dir, attrs);
          }
          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (IOException e) {
        logger.error("Failed to traverse through working directory", e);
        throw new CWL1GlobException("Failed to traverse through working directory", e);
      }
    }
    return files;
  }
  
}
