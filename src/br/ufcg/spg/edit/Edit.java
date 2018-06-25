package br.ufcg.spg.edit;

import br.ufcg.spg.imports.Import;
import br.ufcg.spg.template.TemplateUtils;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "Edit")
@Table(name = "Edit")
public class Edit {
  @Id
  @GeneratedValue
  private Long id;
  private String commit;
  private int startPos;
  private int endPos;
  private int index;
  private String project;
  private String developer;
  private String email;
  @Temporal(TemporalType.DATE)
  private Date date;
  private String path;
  //@Column(columnDefinition = "TEXT")
  //private String pathRoot;
  @Column(columnDefinition = "TEXT")
  private String dcap3;
  @Column(columnDefinition = "TEXT")
  private String dcap2;
  @Column(columnDefinition = "TEXT")
  private String dcap1;
  @Column(columnDefinition = "TEXT")
  private String template;
  @Column(columnDefinition = "TEXT")
  private String text; 
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "context")
  private Edit context;
  
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Edit dst;
  
  //@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  //private Edit upper;
  
  /**
   * List of nodes in this cluster.
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Import> imports;
  
  public Edit() {    
  }
 
  /**
   * Creates a new edit data.
   * @param commit commit
   * @param startPos start position
   * @param endPos end position
   * @param project project
   * @param path path
   * @param context context
   * @param dst destination
   */
  public Edit(final String commit, final int startPos, 
                  final int endPos, final int index, final String project, 
                  final String path, final String pathRoot, final Edit context, final Edit dst,
                  final String template, final String text) {
    super();
    this.commit = commit;
    this.startPos = startPos;
    this.endPos = endPos;
    this.index = index;
    this.project = project;
    this.path = path;
    //this.pathRoot = pathRoot;
    this.context = context;
    this.dst = dst;
    this.template = template;
    this.text = text;
  }
  
  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getCommit() {
    return commit;
  }
  
  public void setCommit(final String commit) {
    this.commit = commit;
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
 
  public int getIndex() {
    return index;
  }

  public void setIndex(final int index) {
    this.index = index;
  }

  public String getProject() {
    return project;
  }
  
  public void setProject(final String project) {
    this.project = project;
  }
  
  public String getPath() {
    return path;
  }
  
  public void setPath(final String path) {
    this.path = path;
  }
  
  /*public String getPathRoot() {
    return pathRoot;
  }

  public void setPathRoot(String pathRoot) {
    this.pathRoot = pathRoot;
  }*/

  public String getDcap3() {
    return dcap3;
  }

  public void setDcap3(final String dcap3) {
    this.dcap3 = dcap3;
  }

  public String getDcap2() {
    return dcap2;
  }

  public void setDcap2(final String dcap2) {
    this.dcap2 = dcap2;
  }

  public String getDcap1() {
    return dcap1;
  }

  public void setDcap1(final String dcap1) {
    this.dcap1 = dcap1;
  }

  public String getPlainTemplate() {
    return TemplateUtils.removeAll(template);
  }
  
  public String getTemplate() {
    return template;
  }

  public void setTemplate(final String template) {
    this.template = template;
  }

  public String getText() {
    return text;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public Edit getContext() {
    return context;
  }

  public void setContext(final Edit context) {
    this.context = context;
  }

  public Edit getDst() {
    return dst;
  }

  public void setDst(final Edit dst) {
    this.dst = dst;
  }
  
  public List<Import> getImports() {
    return imports;
  }

  public void setImports(final List<Import> imports) {
    this.imports = imports;
  }

  public String getDeveloper() {
    return developer;
  }

  public void setDeveloper(final String developer) {
    this.developer = developer;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(final Date date) {
    this.date = date;
  }

  public Edit getUpper() {
    //return upper;
    return null;
  }

  public void setUpper(Edit upper) {
    //this.upper = upper;
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    final String str = this.getText() + ", " + this.getPath() + ", " + this.getCommit() + "\n";
    return str;
  }
}


