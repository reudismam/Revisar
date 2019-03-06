package br.ufcg.spg.project;

import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.os.OpSysUtils;

import java.io.File;
import java.io.IOException;

public class ProjectAnalyzer {
  
  private ProjectAnalyzer() {
  }
  
  /**
   * Configures project.
   * @param project project
   * @param srcFile source list
   * @param dstFile destination list
   * @return project
   */
  public static ProjectInfo project(final String project, 
      final String srcFile, final String dstFile) {
    final String srcFolder = srcFolder(project);
    final String[] classpathSrc = classpath(srcFolder + "sources.txt");
    final String [] sourcesSrc = sources(srcFile);
    final Version src = new Version();
    src.setProject(srcFolder);
    src.setSource(sourcesSrc);
    src.setClasspath(classpathSrc);
    final ProjectInfo pinfo = new ProjectInfo();
    pinfo.setSrcVersion(src);
    final Version dst = new Version();
    final String dstFolder = dstFolder(project);
    dst.setProject(dstFolder);
    final String [] sourcesDst = sources(dstFile);
    dst.setSource(sourcesDst);
    final String[] classpathDst = classpath(dstFolder + "sources.txt");
    dst.setClasspath(classpathDst);
    pinfo.setDstVersion(dst);
    return pinfo;
  }
  
  /**
   * Configures project.
   * @param srcEdit source code edit
   * @return project
   */
  public static ProjectInfo project(final Edit srcEdit) {
    final Edit dstEdit = srcEdit.getDst();
    final String project = dstEdit.getProject();
    final String srcFile = srcEdit.getPath();
    final String dstFile = dstEdit.getPath();
    return project(project, srcFile, dstFile);
  }
  
  private static String srcFolder(final String project) {
    return "../Projects/" + project + "_old/";
  }
  
  private static String dstFolder(final String project) {
    return "../Projects/" + project + "/";
  }
  
  /**
   * Gets source.
   * @param fileName file name
   * @return sources folder
   */
  public static String[] sources(final String fileName) {
    final String srcFolder = fileName.substring(0, fileName.indexOf("src/") + 4);
    final File srcFile = new File(srcFolder);
    try {
      final String[] sourcesSrc = { srcFile.getCanonicalPath() };
      return sourcesSrc;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets source.
   * @return sources folder
   */
  public static String[] sources() {
    return new String [] {"\\"};
  }
  
  /**
   * Get sources files.
   * 
   * @param sourcesPath
   *          source paths
   * @return source files
   */
  public static String[] classpath(final String sourcesPath) {
    String[] classpath;
    if (OpSysUtils.isWindows()) {
      classpath = new String [] { 
        "C:\\Program Files (x86)\\Java\\jre1.8.0_121\\lib\\rt.jar" };
    } else if (OpSysUtils.isMac()) {
      classpath = new String [] {
        "/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/rt.jar"};
    } else if (OpSysUtils.isUnix()) {
      classpath = new String [] {
        "/usr/lib/jvm/java-8-oracle/jre/lib/rt.jar"};
    } else {
      throw new RuntimeException("Could not determine classpath.");
    }
    return classpath;
  }
}
