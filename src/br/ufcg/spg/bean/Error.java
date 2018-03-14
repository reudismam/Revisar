package br.ufcg.spg.bean;

public class Error {
  private int id;
  private int line;
  private String file;
  private String code;
  private int startPosition;
  private int endPosition;
  private String description;

  public Error() {
  }

  /**
   * Constructor.
   * @param id id
   * @param line line
   * @param file file
   * @param code code
   * @param startPosition start position
   * @param endPosition end position
   * @param description description
   */
  public Error(final int id, final int line, final String file, final String code, 
      final int startPosition, final int endPosition, final String description) {
    super();
    this.id = id;
    this.line = line;
    this.file = file;
    this.code = code;
    this.startPosition = startPosition;
    this.endPosition = endPosition;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public void setCode(final String code) {
    this.code = code;
  }

  public void setStartPosition(final int startPosition) {
    this.startPosition = startPosition;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public int getLine() {
    return line;
  }

  public void setLine(final int line) {
    this.line = line;
  }

  public String getFile() {
    return file;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public void setPosition(final int startPosition) {
    this.startPosition = startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

  public void setEndPosition(final int endPosition) {
    this.endPosition = endPosition;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Compares Error objects.
   * @param er other error
   */
  public boolean equals(final Error er) {
    if (line != er.getLine()) {
      return false;
    }
    if (!file.equals(er.getFile())) {
      return false;
    }
    if (!code.equals(er.getCode())) {
      return false;
    }
    if (startPosition != er.getStartPosition()) {
      return false;
    }
    if (!description.equals(er.getDescription())) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final String s = "Id: " + id + "\n" + "Line: " + line + "\n" + "File: " + file + "\n" + "Code: " + code + "\n"
        + "Start position: " + startPosition + "\n" + "End position: " + endPosition + "\n" + "Description: "
        + description;
    return s;
  }
}
