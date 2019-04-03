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

  public void add(CompilationUnit templateClass) {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    if (templateClass.getPackage().toString().contains("java.lang") && classDecl.getName().toString().contains("Class1")) {
      throw new RuntimeException();
    }
    for (CompilationUnit cunit : generated) {
      TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(cunit);
      if ((cunit.getPackage().toString().trim() + typeDeclaration.getName()).equals(
              templateClass.getPackage().toString().trim() + classDecl.getName())) {
        System.out.println(templateClass.getPackage().toString().trim() + classDecl.getName());
        System.out.println(cunit.getPackage().toString().trim() + typeDeclaration.getName());
        throw new RuntimeException("Duplicates");
      }
    }
    generated.add(templateClass);
  }

  public void remove(CompilationUnit templateClass) {
    generated.remove(templateClass);
  }

  public CompilationUnit getClassInRepository(String fullName) {
    CompilationUnit match = null;
    for (CompilationUnit cunit : generated) {
      TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(cunit);
      String search = cunit.getPackage().getName().toString().trim() + "." + typeDeclaration.getName();
      System.out.println("search: " + search);
      System.out.println("full name: " + fullName);
      System.out.println("Is full name equals to search: " + search.equals(fullName));
      if (search.equals(fullName)) {
        match = cunit;
        break;
      }
    }
    return match;
  }
}
