package br.ufcg.spg.project;

public class ProjectInfo {
  private Version srcVersion;
  private Version dstVersion;

  public ProjectInfo() {
  }

  public Version getSrcVersion() {
    return srcVersion;
  }

  public void setSrcVersion(final Version srcVersion) {
    this.srcVersion = srcVersion;
  }

  public Version getDstVersion() {
    return dstVersion;
  }

  public void setDstVersion(final Version dstVersion) {
    this.dstVersion = dstVersion;
  }
}
