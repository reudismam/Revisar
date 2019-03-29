package br.ufcg.spg.type;

import br.ufcg.spg.binding.BindingSolver;

import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.transformation.ImportUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import org.eclipse.jdt.core.dom.*;

public class TypeUtils {
  
  private TypeUtils() {
  }

  /**
   * Extracts the type of the node.
   * @param astNode before node
   * @param ast at
   * @return the type of the node.
   */
  public static Type extractType(final ASTNode astNode, final AST ast) {
    if (astNode instanceof Type) {
      return (Type) astNode;
    }
    // for simple variable declaration
    if (astNode instanceof SingleVariableDeclaration) {
      final SingleVariableDeclaration decl = (SingleVariableDeclaration) astNode;
      return decl.getType();
    }
    if (astNode instanceof VariableDeclarationStatement) {
      final VariableDeclarationStatement stm = (VariableDeclarationStatement) astNode;
      return stm.getType();
    }
    if (astNode instanceof FieldDeclaration) {
      final FieldDeclaration tdecl = (FieldDeclaration) astNode;
      return tdecl.getType();
    }
    if (astNode instanceof TypeParameter) {
      final TypeParameter tparam = (TypeParameter) astNode;
      final WildcardType type = ast.newWildcardType();
      final List<?> boundList = tparam.typeBounds();
      Type bound = null;
      if (!boundList.isEmpty()) {
        bound = (Type) boundList.get(0);
        bound = (Type) ASTNode.copySubtree(ast, bound);
      }
      if (bound != null) {
        type.setBound(bound, true);
      }
      return type;
    }
    // for simple type
    if (astNode instanceof SimpleType) {
      final ASTNode node = TypeUtils.nodeForType(astNode);
      final ITypeBinding typeBinding = BindingSolver.typeBinding(node);
      return TypeUtils.typeFromBinding(ast, typeBinding);
    }
    if (astNode instanceof ArrayType) {
      final ArrayType arr = (ArrayType) astNode;
      final Type type = arr;
      return type;
    }
    // for parameterized type
    if (astNode instanceof ParameterizedType) {
      final ParameterizedType type = (ParameterizedType) astNode;
      return TypeUtils.typeFromParameterizedType(ast, type);
    }
    // TODO: add other types on demand
    final ITypeBinding binding = BindingSolver.typeBinding(astNode);
    if (binding == null) {
      return ast.newPrimitiveType(PrimitiveType.VOID);
    }
    final Type type = TypeUtils.typeFromBinding(ast, binding);
    return type;
  }

  /**
   * Returns the type for binding.
   * 
   * @param ast
   *          ast
   * @param typeBinding
   *          type binding
   * @return returns the type for binding
   */
  @SuppressWarnings("unchecked")
  public static Type typeFromBinding(final AST ast, final ITypeBinding typeBinding) {
    if (ast == null) {
      throw new NullPointerException("ast is null");
    }
    if (typeBinding == null) {
      throw new NullPointerException("typeBinding is null");
    }
    if (typeBinding.isPrimitive()) {
      return ast.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName()));
    }
    if (typeBinding.isTypeVariable()) {
      final WildcardType capType = ast.newWildcardType();
      final ITypeBinding bound = typeBinding.getBound();
      if (bound != null) {
        capType.setBound(typeFromBinding(ast, bound), typeBinding.isUpperbound());
      }
      return capType;
    }
    if (typeBinding.isCapture()) {
      final ITypeBinding wildCard = typeBinding.getWildcard();
      final WildcardType capType = ast.newWildcardType();
      final ITypeBinding bound = wildCard.getBound();
      if (bound != null) {
        capType.setBound(typeFromBinding(ast, bound), wildCard.isUpperbound());
      }
      return capType;
    }
    if (typeBinding.isTypeVariable()) {
      final WildcardType type = ast.newWildcardType();
      final ITypeBinding bound = typeBinding.getBound();
      if (bound != null) {
        type.setBound(typeFromBinding(ast, bound), typeBinding.isUpperbound());
      }
      return type;
    }
    if (typeBinding.isArray()) {
      final Type elType = typeFromBinding(ast, typeBinding.getElementType());
      return ast.newArrayType(elType, typeBinding.getDimensions());
    }
    if (typeBinding.isParameterizedType()) {
      final Type typeErasure = typeFromBinding(ast, typeBinding.getErasure());
      final ParameterizedType type = ast.newParameterizedType(typeErasure);
      final List<Type> newTypeArgs = new ArrayList<>();
      for (final ITypeBinding typeArg : typeBinding.getTypeArguments()) {
        newTypeArgs.add(typeFromBinding(ast, typeArg));
      }
      type.typeArguments().addAll(newTypeArgs);
      return type;
    }
    if (typeBinding.isWildcardType()) {
      final WildcardType type = ast.newWildcardType();
      return type;
    }
    // simple or raw type
    final String qualName = typeBinding.getQualifiedName();
    if ("".equals(qualName)) {
      throw new IllegalArgumentException("No name for type binding.");
    }
    return ast.newSimpleType(ast.newName(qualName));
  }

  public static Type typeFromParameterizedType(final AST ast, final ParameterizedType param) {
    final Type type = (Type) ASTNode.copySubtree(ast, param);
    return type;
  }

  /**
   * Returns the name for the simple type.
   * 
   * @param type
   *          simple type
   * @return name for the simple type
   */
  public static ASTNode nodeForType(final ASTNode type) {
    if (type.getNodeType() == ASTNode.SIMPLE_TYPE) {
      final SimpleType smType = (SimpleType) type;
      final ASTNode name = smType.getName();
      return name;
    }
    return type;
  }

  /**
   * Extract types.
   */
  public static List<Type> extractTypes(List<ASTNode> targetList, 
      final AST refasterRule) {
    final List<Type> paramTypes = new ArrayList<>();
    for (int i = 0; i < targetList.size(); i++) {
      final ASTNode tbefore = targetList.get(i);
      final Type paramType = extractType(tbefore, refasterRule);
      paramTypes.add(paramType);
    }
    return paramTypes;
  }

  /**
   * Filter types.
   * @param nodes to be filtered
   * @return index of nodes to be removed
   */
  public static List<Integer> filterTypes(List<ASTNode> nodes) {
    try {
      List<Integer> toRemove = new ArrayList<>();
      for (int i = 0; i < nodes.size(); i++) {
        if (nodes.get(i).getParent() instanceof MethodInvocation) {
          MethodInvocation parent = (MethodInvocation) nodes.get(i).getParent();
          if (parent.getName().equals(nodes.get(i))) {
            toRemove.add(i);
          }
        }
        if (!(nodes.get(i) instanceof Type)) {
          continue;
        }
        Type tmp = (Type) nodes.get(i);
        if (tmp.isParameterizedType() || tmp.isPrimitiveType() || tmp.isArrayType() 
            || tmp.isAnnotatable() || tmp.isIntersectionType() || tmp.isNameQualifiedType() 
            || tmp.isSimpleType() || tmp.isQualifiedType() || tmp.isUnionType() 
            || tmp.isWildcardType()) {
          toRemove.add(i);
        }
      }
      return toRemove;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Type> createGenericParamTypes(Type leftHandSideClass) {
    AST ast = leftHandSideClass.getAST();
    List<Type> genericParamTypes = new ArrayList<>();
    if (leftHandSideClass.isParameterizedType()) {
      ParameterizedType paramType = (ParameterizedType) leftHandSideClass;
      List<Type> typeArguments = paramType.typeArguments();
      char letter = 'T';
      for (Type type : typeArguments) {
        Name name = ast.newName(String.valueOf(letter ++));
        SimpleType simpleType = ast.newSimpleType(name);
        genericParamTypes.add(simpleType);
      }
      paramType.typeArguments().clear();
      for (Type type : genericParamTypes) {
        type = (Type) ASTNode.copySubtree(paramType.getAST(), type);
        typeArguments.add(type);
      }
    }
    return genericParamTypes;
  }

  public static Type getClassType(CompilationUnit unit, ASTNode node) {
    Type classType = TypeUtils.extractType(node, node.getAST());
    boolean isStatic = classType.toString().equals("void");
    if (isStatic) {
      String typeName = JDTElementUtils.extractSimpleName(node.toString());
      classType = ImportUtils.getTypeBasedOnImports(unit, typeName);
    }
    return classType;
  }
}
