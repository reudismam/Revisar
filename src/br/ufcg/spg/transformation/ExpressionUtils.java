package br.ufcg.spg.transformation;

import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;

public class ExpressionUtils {
  public static void processExpression(CompilationUnit unit, Expression initializer, Type type) throws IOException {
    processExpressionBase(unit, type, initializer);
    if (initializer instanceof ConditionalExpression) {
      ConditionalExpression conditionalExpression  = (ConditionalExpression) initializer;
      processExpressionBase(unit, type, conditionalExpression.getThenExpression());
      processExpressionBase(unit, type, conditionalExpression.getElseExpression());
    }
    else if(initializer instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) initializer;
      Type typeObject = TypeUtils.extractType(castExpression.getExpression(), initializer.getAST());
      processExpressionBase(unit, typeObject, castExpression.getExpression());
      Type typeCast = TypeUtils.extractType(initializer, initializer.getAST());
      String nameObject = NameUtils.extractSimpleName(typeObject);
      typeObject = ImportUtils.getTypeBasedOnImports(unit, nameObject);
      String castName = NameUtils.extractSimpleName(typeCast);
      typeCast = ImportUtils.getTypeBasedOnImports(unit, castName);
      CompilationUnit templateClass = ClassUtils.getTemplateClass(unit, typeObject);
      TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateClass);
      typeCast = (Type) ASTNode.copySubtree(typeDeclaration.getAST(), typeCast);
      typeDeclaration.setSuperclassType(typeCast);
    }
    else if (initializer instanceof InfixExpression) {
      InfixExpression infixExpression = (InfixExpression) initializer;
      InfixExpression.Operator operator = infixExpression.getOperator();
      if (operator.equals(InfixExpression.Operator.EQUALS)
              || (operator.equals(InfixExpression.Operator.NOT_EQUALS))) {
        Type newtype = SyntheticClassUtils.getSyntheticType(unit.getAST());
        processLeftAndRight(unit, infixExpression, newtype);
      } else if (operator.equals(InfixExpression.Operator.CONDITIONAL_AND)
              || (operator.equals(InfixExpression.Operator.CONDITIONAL_OR))) {
        Type newtype = unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN);
        processLeftAndRight(unit, infixExpression, newtype);
      } else if (operator.equals(InfixExpression.Operator.TIMES)
              || operator.equals(InfixExpression.Operator.DIVIDE)
              || operator.equals(InfixExpression.Operator.PLUS)
              || operator.equals(InfixExpression.Operator.REMAINDER)
              || operator.equals(InfixExpression.Operator.MINUS)) {
        if (type != null) {
          processLeftAndRight(unit, infixExpression, type);
        }
      }
    }
  }

  private static void processLeftAndRight(CompilationUnit unit, InfixExpression infixExpression, Type returnType) throws IOException {
    if (infixExpression.getLeftOperand() instanceof MethodInvocation) {
      processExpressionBase(unit, returnType, infixExpression.getLeftOperand());
    } else if (infixExpression.getRightOperand() instanceof  MethodInvocation) {
      processExpressionBase(unit, returnType, infixExpression.getRightOperand());
    }
  }


  public static void processExpressionBase(CompilationUnit unit, Type type, Expression initializer) throws IOException {
    if (initializer instanceof MethodInvocation) {
      StubUtils.processMethodInvocation(unit, type, initializer);
    }
    else if (initializer instanceof ClassInstanceCreation) {
      StubUtils.processClassCreation(unit, type, initializer);
    }
    else if (initializer instanceof  FieldAccess) {
      FieldDeclarationUtils.processFieldDeclaration(unit, type, initializer);
    }
  }
}
