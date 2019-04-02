package br.ufcg.spg.transformation;

import br.ufcg.spg.refaster.ClassUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ClassRepository {
  private static ClassRepository instance;

  private List<CompilationUnit> generated;

  private ClassRepository() {
    generated = new ArrayList<>();
  }

  public static ClassRepository getInstance() {
    if (instance == null) {
      instance = new ClassRepository();
    }
    return instance;
  }

  public List<CompilationUnit> getGenerated() {
    return generated;
  }

  public CompilationUnit getClassInRepository(String className) {
    for (CompilationUnit unit : generated) {
      TypeDeclaration declaration = ClassUtils.getTypeDeclaration(unit);
      if (declaration.getName().toString().equals(className)) {
        return unit;
      }
    }
    return null;
  }
}
