package br.ufcg.spg.cli;

public class CheckStyleReport {
  private String kind;
  private String file;
  private int line;
  private String message;
  private String classification;

  public CheckStyleReport(String kind, String file, int line, String message, String classification) {
    this.kind = kind;
    this.file = file;
    this.line = line;
    this.message = message;
    this.classification = classification;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getClassification() {
    return classification;
  }

  public void setClassification(String classification) {
    this.classification = classification;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Kind: " + kind).append('\n');
    sb.append("File: " + file).append('\n');
    sb.append("Line: " + line).append('\n');
    sb.append("Message: " + message).append('\n');
    sb.append("Classification: " + classification).append('\n');
    return sb.toString();
  }
}
