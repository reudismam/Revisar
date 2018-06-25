package br.ufcg.spg.ml.editoperation;

public class DeleteNode implements IEditNode {
  private String parent;
  private String node;

  public DeleteNode(String parent, String node) {
    this.parent = parent;
    this.node = node;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((node == null) ? 0 : node.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DeleteNode other = (DeleteNode) obj;
    if (node == null) {
      if (other.node != null)
        return false;
    } else if (!node.equals(other.node))
      return false;
    if (parent == null) {
      if (other.parent != null)
        return false;
    } else if (!parent.equals(other.parent))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DeleteNode [parent=" + parent + ", node=" + node + "]";
  }
}
