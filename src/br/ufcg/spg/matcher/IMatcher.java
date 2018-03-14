package br.ufcg.spg.matcher;

public interface IMatcher<T> {
  boolean evaluate(T t);
}
