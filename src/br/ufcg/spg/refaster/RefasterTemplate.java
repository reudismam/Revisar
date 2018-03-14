package br.ufcg.spg.refaster;

import com.google.errorprone.refaster.annotation.*;

public class RefasterTemplate {
  @BeforeTemplate
  Object before() {
    return null;
  }
  
  @AfterTemplate 
  Object after() {
    return null;
  }
}
