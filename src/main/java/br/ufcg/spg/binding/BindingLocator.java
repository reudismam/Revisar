package br.ufcg.spg.binding;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;

public class BindingLocator {

  /**
   * Resolves biding.
   */
  public static void resolveBinding(final IJavaElement javaElement) 
      throws CoreException {
    /*
     * //IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new
     * IJavaElement[] { javaElement }); IJavaSearchScope scope =
     * SearchEngine.createWorkspaceScope(); // Use // this if you dont have the
     * IProject in hand SearchPattern searchPattern =
     * SearchPattern.createPattern(null, IJavaSearchConstants.REFERENCES);
     * SearchRequestor requestor = new SearchRequestor() {
     * 
     * @Override public void acceptSearchMatch(SearchMatch match) throws
     * CoreException { System.out.println(match.getElement()); } }; SearchEngine
     * searchEngine = new SearchEngine(); searchEngine.search(searchPattern, new
     * SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
     * scope, requestor, new NullProgressMonitor());
     */
    /*
     * SearchEngine engine = new SearchEngine(); IJavaSearchScope workspaceScope
     * = SearchEngine .createWorkspaceScope();
     * 
     * int a = workspaceScope.REFERENCED_PROJECTS; SearchPattern pattern =
     * SearchPattern.createPattern("Test", IJavaSearchConstants.TYPE,
     * IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
     * IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
     * SearchRequestor requestor = new SearchRequestor() { public void
     * acceptSearchMatch(SearchMatch match) { System.out.println("Found: " +
     * match.getElement()); } }; SearchEngine searchEngine = new SearchEngine();
     * try { searchEngine.search(pattern, new SearchParticipant[] {
     * SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null); }
     * catch (CoreException e) { e.printStackTrace(); }
     */
  }
}
