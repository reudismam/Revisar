package br.ufcg.spg.refaster;

import com.google.errorprone.refaster.*;

public class RefasterTemplates {

  Object clazz() {
    return Refaster.<Object>clazz();
  }
}
