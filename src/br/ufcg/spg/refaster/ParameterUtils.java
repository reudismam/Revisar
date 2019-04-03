package br.ufcg.spg.refaster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.transformation.*;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

/**
 * Configure parameters.
 */
public final class ParameterUtils {
  
  private ParameterUtils() {
  }
  
  /**
   * Adds parameter to method.
   * @param types types to be analyzed
   * @param cuUnit compilation unit
   * @param method method 
   * @return method with parameters added.
   */
  @SuppressWarnings("unchecked")
  public static MethodDeclaration addParameter(final List<Type> types, List<String> varNames,
      final CompilationUnit cuUnit, MethodDeclaration method) {
    final AST ast = cuUnit.getAST();
    final List<ASTNode> parameters = new ArrayList<>();
    for (int i = 0; i < types.size(); i++) {
      // Create a new variable declaration to be added as parameter.
      final SingleVariableDeclaration singleVariableDeclaration = 
          ast.newSingleVariableDeclaration();
      final SimpleName name = ast.newSimpleName(varNames.get(i));
      singleVariableDeclaration.setName(name);
      Type type = types.get(i);
      type = (Type) ASTNode.copySubtree(ast, type);
      singleVariableDeclaration.setType(type);
      singleVariableDeclaration.setVarargs(false);
      final ASTNode singleVariableDeclarationCopy = ASTNode.copySubtree(
          ast, singleVariableDeclaration);
      parameters.add(singleVariableDeclarationCopy);
    }
    method = (MethodDeclaration) ASTNode.copySubtree(ast, method);
    method.parameters().addAll(parameters);
    return method;
  }

  public static MethodDeclaration addParameters(CompilationUnit unit,
                                                List<ASTNode> arguments, CompilationUnit templateClass, MethodDeclaration mDecl) throws IOException {
    List<Type> argTypes = new ArrayList<>();
    List<String> varNames = new ArrayList<>();
    int i = 0;
    for(ASTNode argNode : arguments) {
      varNames.add("v_" + i++);
      Expression arg = (Expression) argNode;
      Type argType;
      if (arg.toString().equals("null")) {
        argType = TypeUtils.createType(unit.getAST(), "java.lang", "Object");
      } else {
        argType = TypeUtils.extractType(arg, arg.getAST());
      }
      if (arg instanceof MethodInvocation) {
        try {
        if (argType.toString().equals("void")) {
          argType = SyntheticClassUtils.getSyntheticType(unit.getAST());
          CompilationUnit synthetic = SyntheticClassUtils.createSyntheticClass(unit);
          JDTElementUtils.saveClass(synthetic);
        }
          StubUtils.processMethodInvocation(unit, argType, arg);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      else if (arg instanceof ClassInstanceCreation) {
        ClassInstanceCreation initializer = (ClassInstanceCreation) arg;
        Type type = TypeUtils.extractType(arg, arg.getAST());
        try {
          StubUtils.processClassCreation(unit, type, initializer);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      else if (arg instanceof QualifiedName) {
        QualifiedName qualifiedName = (QualifiedName) arg;
        System.out.println(qualifiedName.getName() + " : " + qualifiedName.getQualifier());
        ASTNode imp = ImportUtils.findImport(unit, qualifiedName.getQualifier().toString());
        Tuple<String, String> inner = InnerClassUtils.getInnerClassImport(qualifiedName.getQualifier().toString());;
        String fullName;
        if (imp != null) {
           fullName = imp.toString().substring(7, imp.toString().indexOf(inner.getItem1())-1);
        }
        else {
           fullName = "defaultpkg." + qualifiedName.getQualifier();
           System.out.println(fullName);
        }
        StubUtils.processInnerClass(unit, inner, fullName);
        Type type = SyntheticClassUtils.getSyntheticType(unit.getAST());
        FieldDeclaration fieldDeclaration = FieldDeclarationUtils.createFieldDeclaration(unit, qualifiedName.getName(), type);
        FieldDeclarationUtils.addModifier(fieldDeclaration, Modifier.ModifierKeyword.STATIC_KEYWORD);
        Type typeOuter = TypeUtils.createType(unit.getAST(), fullName, inner.getItem1());
        Type typeInner = TypeUtils.createType(unit.getAST(), fullName, inner.getItem2());
        CompilationUnit outer = ClassUtils.getTemplateClass(unit, typeOuter);
        CompilationUnit innerClass = ClassUtils.getTemplateClass(unit, typeInner);
        fieldDeclaration = (FieldDeclaration) ASTNode.copySubtree(innerClass.getAST(), fieldDeclaration);
        TypeDeclaration declaration = ClassUtils.getTypeDeclaration(innerClass);
        declaration.bodyDeclarations().add(fieldDeclaration);
        TypeDeclaration outerTypeDeclaration = ClassUtils.getTypeDeclaration(outer);
        List<ASTNode> nodes = (List<ASTNode>)  outerTypeDeclaration.bodyDeclarations();
        ASTNode toRemove = null;
        for (ASTNode node : nodes) {
          if (node instanceof TypeDeclaration) {
            if(((TypeDeclaration) node).getName().toString().equals(declaration.getName().toString())) {
              toRemove = node;
            }
          }
        }
        nodes.remove(toRemove);
        declaration = (TypeDeclaration) ASTNode.copySubtree(outerTypeDeclaration.getAST(), declaration);
        outerTypeDeclaration.bodyDeclarations().add(declaration);
        System.out.println(outer);
        System.out.println(innerClass);
        argType = type;
      }
      //Needed to resolve a bug in eclipse JDT.
      if (argType.toString().contains(".") && !argType.toString().contains("syntethic")) {
        System.out.println(argType);
        String typeName = JDTElementUtils.extractSimpleName(argType);
        System.out.println(typeName);
        argType = ImportUtils.getTypeBasedOnImports(unit, typeName);
      }
      System.out.println(argNode + " : " + argType);
      argTypes.add(argType);
    }
    mDecl = addParameter(argTypes, varNames, templateClass, mDecl);
    return mDecl;
  }
}
