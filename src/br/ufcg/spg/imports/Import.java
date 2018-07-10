package br.ufcg.spg.imports;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: Import.
 *
 */
@Entity(name = "Import")
@Table(name = "Import")
public class Import {
  
  /**
   * Unit id.
   */
  @Id
  @GeneratedValue
  private Long id;
  
  /**
   * Start position.
   */
  private int startPos;
  
  /**
   * End position.
   */
  private int endPos;
  
  @Column(columnDefinition = "TEXT")
  private String text;
  
  /**
   * Creates a new instance.
   */
  public Import() {
    super();
  }
  
  /**
   * Constructor.
   */
  public Import(final int startPos, final int endPos, final String text) {
    super();
    this.startPos = startPos;
    this.endPos = endPos;
    this.text = text;
  }

  public Long getId() {
    return id;
  }
  
  public void setId(final Long id) {
    this.id = id;
  }

  public int getStartPos() {
    return startPos;
  }

  public void setStartPos(final int startPos) {
    this.startPos = startPos;
  }

  public int getEndPos() {
    return endPos;
  }

  public void setEndPos(final int endPos) {
    this.endPos = endPos;
  }

  public String getText() {
    return text;
  }

  public void setText(final String text) {
    this.text = text;
  }
}
