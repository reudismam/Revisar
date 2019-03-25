package br.ufcg.spg.stub;

import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.refaster.TemplateConstants;
import br.ufcg.spg.refaster.config.ClassUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.type.TypeUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StubUtils {

  public static void generateStubsForClass(String classFile) {
    try {
      CompilationUnit unit = JParser.parseFromFile(classFile);
      FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
      IMatcher<ASTNode> matcher = new KindNodeMatcher(ASTNode.VARIABLE_DECLARATION_STATEMENT);
      MatchCalculator<ASTNode> match = new NodeMatchCalculator(matcher);
      List<ASTNode> invocations = match.getNodes(unit);
      System.out.println(unit.toString());
      for(ASTNode ast : invocations){
        VariableDeclarationStatement inv = (VariableDeclarationStatement) ast;
        System.out.println(inv + " : " + TypeUtils.extractType(inv, inv.getAST()));
      }
      for (ASTNode node : invocations) {
        VariableDeclarationStatement inv = (VariableDeclarationStatement) node;
        List<VariableDeclarationFragment> fragments = inv.fragments();
        for (VariableDeclarationFragment flag : fragments) {
          Expression initializer = flag.getInitializer();
          if (initializer instanceof MethodInvocation) {
            Type type = TypeUtils.extractType(inv, inv.getAST());
            //MethodInvocation invocation = (MethodInvocation) initializer;
            //CompilationUnit templateClass = getTemplateClass(unit, invocation.getExpression());
            //MethodInvocationStub.stubForMethodInvocation(unit, templateClass, type, initializer);
          }
          else if (initializer instanceof ClassInstanceCreation) {
            System.out.print("A class instance creation: ");
            CompilationUnit templateClass = getTemplateClass(unit, initializer);
            ClassInstanceCreation instance = (ClassInstanceCreation) initializer;
            ClassInstanceCreationStub.stubForMethodInvocation(unit, templateClass, instance, inv);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static CompilationUnit getTemplateClass(CompilationUnit unit, ASTNode node) throws IOException {
    CompilationUnit templateClass;
    AST ast = node.getAST();
    Type classType = TypeUtils.extractType(node, ast);
    boolean isStatic = classType.toString().equals("void");
    if (isStatic) {
      classType = StubUtils.getTypeBasedOnImports(null, unit, node.toString());
    }
    String baseName = JDTElementUtils.extractSimpleName(classType);
    ASTNode imp = StubUtils.findImport(unit, baseName);
    Type packageType = StubUtils.getTypeFromImport(baseName, ast, imp);
    String pkg = packageType.toString().replaceAll("\\.", "/") + baseName + ".java";
    if ((new File(pkg).exists())) {
      templateClass = JParser.parseFromFile(pkg);
    }
    else {
      templateClass = JParser.parseFromFile(TemplateConstants.ClassPath);
      TypeDeclaration classDecl = ClassUtils.createClassDeclaration(templateClass, baseName, imp);
      String typeStr = classType.toString().substring(classType.toString().lastIndexOf(".") + 1);
      System.out.println(classType.toString());
      SimpleName simpleName = classDecl.getAST().newSimpleName(typeStr);
      JDTElementUtils.setName(classDecl, simpleName);
    }
    return templateClass;
  }

  public static Type getTypeBasedOnImports(TypeDeclaration classDecl, CompilationUnit unit, String importedType) {
    AST ast = unit.getAST();
    ASTNode imp = findImport(unit, importedType);
    System.out.println("Imported type: " + importedType);
    return getTypeFromImport(importedType, ast, imp);
  }

  public static Type getTypeFromImport(String importedType, AST ast, ASTNode imp) {
    Name name = ast.newName(imp.toString().substring(7, imp.toString().lastIndexOf(".")));
    SimpleName sname = ast.newSimpleName(importedType);
    return ast.newNameQualifiedType(name, sname);
  }

  public static ASTNode findImport(CompilationUnit unit, String typeName) {
    KindNodeMatcher matcher = new KindNodeMatcher(ASTNode.IMPORT_DECLARATION);
    NodeMatchCalculator calculator = new NodeMatchCalculator(matcher);
    List<ASTNode> imports = calculator.getNodes(unit);
    for (ASTNode imp : imports) {
      String impStr = imp.toString();
      String imported = impStr.substring(impStr.lastIndexOf(".") + 1, impStr.length() - 2);
      if (imported.equals(typeName)) {
        return imp;
      }
    }
    return null;
  }
}
