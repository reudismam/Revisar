package br.ufcg.spg.os;

public class OpSysUtils {

  private static String OS = System.getProperty("os.name").toLowerCase();
  
  /**
   * Verifies if the OS is Windows.
   * 
   * @return true if OS is Windows
   */
  public static boolean isWindows() {
    return (OS.indexOf("win") >= 0);
  }

  /**
   * Verifies if the OS is Mac.
   * @return true if the OS is Mac.
   */
  public static boolean isMac() {
    return (OS.indexOf("mac") >= 0);
  }

  /**
   * Verifies if the OS is Unix.
   * @return true if the OS is Unix
   */
  public static boolean isUnix() {
    return (OS.indexOf("nux") >= 0);
  }
}
