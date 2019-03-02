package br.ufcg.spg.filter;

import at.unisalzburg.dbresearch.apted.node.StringNodeData;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterFormatter;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.transformation.Transformation;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public final class FilterManager {
  
  private FilterManager() {
  }

  /**
   * Verifies if a transformation is noise.
   */
  public static boolean isNoise(final String folderPath, 
      final Transformation trans, final Cluster clusteri,
      final Cluster clusterj) throws IOException {
    if ("a".equals("a")) {
      return false;
    }
    if (FilterManager.isSameBeforeAfter(clusteri)) {
      return true;
    } 
    List<PatternFilter> filters = FilterManager.filterFactory();
    for (PatternFilter filter : filters) {
      final String srcOutput =  clusteri.getAu();
      final String dstOutput = clusterj.getAu();
      if (filter.match(srcOutput, dstOutput)) {
        String counterFormated =  String.format("%03d", 
            TransformationUtils.incrementClusterIndex());
        String path = folderPath + "filtered/" + trans.isValid() 
            + '/' + counterFormated + ".txt";
        final File clusterFile = new File(path);
        StringBuilder content = ClusterFormatter.getInstance()
            .formatCluster(clusteri, clusterj, "");
        FileUtils.writeStringToFile(clusterFile, content.toString());
        return true;
      }
    }
    return false;
  }
  
  /**
   * Evaluates whether the script is noise.
   */
  public static boolean isNoise(Script<StringNodeData> script) {
    if /*(filterUpdate(script)) {
      return true;
    } else if*/ (filterInsert(script)) {
      return true;
    } /*else if (filterRemoveQualifiedName(script)) {
      return true;
    } else if (filterUpdateHash(script)) {
      return true;
    } else if (filterObject(script)) {
      return true;
    }*/ else if (filterMultipleUpdates(script)) {
      return true;
    } else if (filterBoolean(script)) {
      return true;
    } else if (filterNull(script)) {
      return true;
    } else if (filterNumber(script)) {
      return true;
    } else if (filterReturn(script)) {
      return true;
    } else if (filterString(script)) {
      return verifyString(script);
    } else if (filterDelete(script)) {
      return true;
    } /*else if (filterQualified(script)) {
      return true;
    }*/
    return filterRemoveName(script);
  }
  
  private static boolean filterMultipleUpdates(Script<StringNodeData> script) {
    for (EditNode<StringNodeData> edit : script.getList()) {
      if (edit.toString().matches("Update\\(hash_[0-9]+ to [a-z0-9]+[A-Za-z0-9]*\\)")) {
        if (edit.toString().matches("Update\\(hash_[0-9]+ to (int|float|long|double|rawtypes|[0-9]+[A-Za-z])\\)")) {
          return false;
        }
      } else if (edit.toString().matches("Update\\(hash_[0-9]+ to hash_[0-9]+\\)")) {
        continue;
      } else if (edit.toString()
          .matches("Update\\(hash_[0-9]+ to "
              //+ "(Object|Before|After|Test|Collection|Long|_STRING|[A-Za-z]*Error|String|[a-zA-Z]*Exception)\\)")) {
              + "(Object|Before|After|Test|Long|_STRING)\\)")) {
        continue;
      } else {
        return false;
      }
    }
    return true;
  }
  
  /*private static boolean filterUpdate(Script<StringNodeData> script) {
    if (script.getList().size() == 1) {
      return script.getList().get(0).toString()
          .matches("Update\\(hash_[0-9]+ to [a-z0-9]+[A-Za-z0-9]*\\)");
    }
    return false;
  }*/
  
  private static boolean filterDelete(Script<StringNodeData> script) {
    if (script.getList().size() == 1) {
      return script.getList().get(0).toString()
          .matches("Delete\\(hash_[0-9]+\\)");
    }
    return false;
  }
  
  /*
  private static boolean filterUpdateHash(Script<StringNodeData> script) {
    if (script.getList().size() == 1) {
      return script.getList().get(0).toString().matches("Update\\(hash_[0-9]+ to hash_[0-9]+\\)");
    }
    return script.getList().isEmpty();
  }*/
  
  /*private static boolean filterObject(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString()
          .matches("Update\\(hash_[0-9]+ to (Object|String|[a-zA-Z]*Exception)\\)")) {
        return true;
      }
    }
    return false;
  }*/
  
  private static boolean filterBoolean(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString()
          .matches("Insert\\((true|false), BOOLEAN_LITERAL, [0-9]+\\)")
          || edit.toString().matches("Delete\\(BOOLEAN_LITERAL\\)")
          || edit.toString().matches("Update\\((hash_[0-9]+|true|false) to (true|false)\\)")) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean filterNull(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (/*edit.toString()
          .matches("Insert\\(null, NULL_LITERAL, [0-9]+\\)") 
          ||*/ edit.toString().matches("Update\\(.+ to null\\)")
          || edit.toString().matches("Update\\(null to .+\\)")
          || edit.toString().matches("Delete\\(null\\)")) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean filterNumber(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString()
          .matches("Insert\\([0-9]+, NUMBER_LITERAL, [0-9]+\\)")
          || edit.toString().matches("Delete\\(NUMBER_LITERAL\\)")
          || edit.toString().matches("Update\\(.+ to [0-9]+\\)")) {
        return true;
      }
    }
    return false;
  }

  private static boolean filterString(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString().matches("Delete\\(STRING_LITERAL\\)")) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean verifyString(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString().matches("Delete\\(STRING_LITERAL\\)")) {
        if (i + 1 < list.size()) {
          if (list.get(i + 1).toString().matches("Delete\\(ARRAY_INITIALIZER\\)")
              || list.get(i + 1).toString().matches("Delete\\(SINGLE_MEMBER_ANNOTATION\\)")) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }
  
  /*private static boolean filterQualified(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString()
          .matches("Insert\\(SIMPLE_NAME, QUALIFIED_NAME, [0-9]+\\)")) {
        return true;
      }
    }
    return false;
  }*/
  
  private static boolean filterInsert(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString()
          .matches("Insert\\(METHOD_INVOCATION, VARIABLE_DECLARATION_FRAGMENT, [0-9]+\\)")) {
        return true;
      }
    }
    return false;
  }

  private static boolean filterReturn(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString()
          .matches("Update\\(.+ to return\\)") 
          || edit.toString().matches("Update\\(return to .+\\)")) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean filterRemoveName(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString().matches("Delete\\(hash_[0-9]+\\)") 
          && i + 1 < list.size()) {
        if (list.get(i + 1).toString().matches("Delete\\(SIMPLE_NAME\\)")) {
          if (i + 2 < list.size() && (list.get(i + 2)
              .toString().matches("Delete\\(QUALIFIED_NAME\\)") 
              /*|| list.get(i + 2).toString().matches("Delete\\(SIMPLE_TYPE\\)")*/)) {
            return false;
          }
          if (list.size() == 3 && list.get(2).toString().matches("Delete\\(SIMPLE_TYPE\\)")) {
            return false;
          }
          if (i + 3 < list.size() && list.get(i + 3).toString()
              .matches("Delete\\(PARAMETERIZED_TYPE\\)")) {
            return false;
          }
          if (i + 3 < list.size() && list.get(i + 3).toString()
              .matches("Delete\\(CAST_EXPRESSION\\)")) {
            return false;
          }
          return true;
        }
      }
    }
    return false;
  }
  
  /*private static boolean filterRemoveQualifiedName(Script<StringNodeData> script) {
    List<EditNode<StringNodeData>> list = script.getList();
    for (int i = 0; i < list.size(); i++) {
      EditNode<StringNodeData> edit = list.get(i);
      if (edit.toString().matches("Delete\\(hash_[0-9]+\\)") 
          && i + 1 < list.size()) {
        if (list.get(i + 1).toString().matches("Delete\\(QUALIFIED_NAME\\)")) {
          return true;
        }
      }
    }
    return false;
  }*/

  private static List<PatternFilter> filterFactory() {
    List<PatternFilter> pfilters = new ArrayList<>();
    PatternFilter varrename = new PatternFilter(
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\(hash_[0-9]+\\)\\)", 
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)");
    
    PatternFilter trueFalse = new PatternFilter(
        "RETURN_STATEMENT\\([A-Z]+_[A-Z]+\\([a-zA-Z0-9_]+\\)\\)", 
        "RETURN_STATEMENT\\([A-Z]+_[A-Z]+\\([a-zA-Z0-9_]+\\)\\)");
    
    PatternFilter trueFalseVariable = new PatternFilter(
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME"
        + "\\([_0-9a-zA-Z]+\\), BOOLEAN_LITERAL\\([a-z]+\\)\\)", 
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME"
        + "\\([_0-9a-zA-Z]+\\), BOOLEAN_LITERAL\\([a-z]+\\)\\)");
    
    PatternFilter changeNumber = new PatternFilter(
        "VARIABLE_DECLARATION_FRAGMENT"
        + "\\(SIMPLE_NAME\\(hash_[0-9]+\\), NUMBER_LITERAL\\(hash_[0-9]+\\)\\)", 
        "VARIABLE_DECLARATION_FRAGMENT"
        + "\\(SIMPLE_NAME\\([_0-9A-Za-z]+\\), NUMBER_LITERAL\\([0-9]+\\)\\)");
    
    PatternFilter constructor = new PatternFilter(
        "SUPER_CONSTRUCTOR_INVOCATION\\([ _,a-zA-Z0-9\\(\\)]+\\)", 
        "SUPER_CONSTRUCTOR_INVOCATION\\([ _,a-zA-Z0-9\\(\\)]+\\)");
    
    PatternFilter swithcaseDefault = new PatternFilter(
        "SWITCH_CASE\\([\\(\\)_a-zA-Z0-9]+\\)", 
        "SWITCH_CASE\\([\\(\\)_a-zA-Z0-9]+\\)");
    
    PatternFilter markerAnnotationFilter = new PatternFilter(
        "MARKER_ANNOTATION\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)",
        "MARKER_ANNOTATION\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)");
  
    PatternFilter toSingleVariable = new PatternFilter(
        ".+", 
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)");
    
    PatternFilter infixes = new PatternFilter(
        "(INFIX_EXPRESSION|PREFIX_EXPRESSION|POSTFIX_EXPRESSION)\\([, a-zA-Z0-9\\)\\(_]+\\)", 
        "(INFIX_EXPRESSION|PREFIX_EXPRESSION|POSTFIX_EXPRESSION)\\([, a-zA-Z0-9\\)\\(_]+\\)");
    pfilters.add(varrename);
    pfilters.add(trueFalse);
    pfilters.add(changeNumber);
    pfilters.add(trueFalseVariable);
    pfilters.add(swithcaseDefault);
    pfilters.add(constructor);
    pfilters.add(markerAnnotationFilter);
    pfilters.add(toSingleVariable);
    pfilters.add(infixes);
    return pfilters;
  }

  /**
   * Verifies if the before and after node is identical
   * @param cluster cluster to be analyzed.
   */
  public static boolean isSameBeforeAfter(final Cluster cluster) {
    for (Edit c : cluster.getNodes()) {
      Edit dstEdit = c.getDst();
      if (!c.getText().equals(dstEdit.getText())) {
        return false;
      }
    }
    return true;
  }
}
