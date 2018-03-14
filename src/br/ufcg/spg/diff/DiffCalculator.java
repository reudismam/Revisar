package br.ufcg.spg.diff;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.TreeContext;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Diff calculator.
 */
public abstract class DiffCalculator {
  
  /**
   * Kind of matcher.
   */
  protected Matcher matcher;
  
  /**
   * Source code representation.
   */
  protected TreeContext src;
  
  /**
   * Destination code representation.
   */
  protected TreeContext dst;
  
  /**
   * List of actions.
   */
  private transient List<Action> actions;

  /**
   * Gets matcher.
   * @return matcher
   */
  public Matcher getMatcher() {
    return matcher;
  }

  /**
   * Sets matcher.
   * @param matcher matcher
   */
  public void setMatcher(final Matcher matcher) {
    this.matcher = matcher;
  }

  /**
   * Gets source.
   * @return source
   */
  public TreeContext getSrc() {
    return src;
  }

  /**
   * Sets source.
   * @param src source
   */
  public void setSrc(final TreeContext src) {
    this.src = src;
  }

  /**
   * Gets destination.
   * @return destination
   */
  public TreeContext getDst() {
    return dst;
  }

  /**
   * Sets destination.
   * @param dst destination
   */
  public void setDst(final TreeContext dst) {
    this.dst = dst;
  }
  
  /**
   * Gets the difference between source code and destination code.
   */
  public List<Action> diff() {
    tryDiff();
    if (actions != null) {
      return actions;
    }
    throw new RuntimeException("Larger diff.");
  }
  
  /**
   * Computes diff.
   * @return diff
   */
  protected abstract List<Action> computeDiff();

  /**
   * Creates generator.
   * 
   * @param file
   *          : path to the source file.
   * @return generator.
   */
  public static TreeContext generator(final String file) {
    Run.initGenerators();
    try {
      final TreeContext context = Generators.getInstance().getTree(file);
      return context;
    } catch (UnsupportedOperationException | IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Try to unify eq1 and eq2.
   */
  public void tryDiff() {
    final ExecutorService executor = Executors.newFixedThreadPool(4);   
    final Future<?> future = executor.submit(new Runnable() {
      /**
       * Run method.
       */
      @Override
      public void run() {
        actions = computeDiff();
      }
    });
    executor.shutdown(); // <-- reject all further submissions
    try {
      future.get(2, TimeUnit.SECONDS); // <-- wait 2 seconds to finish
    } catch (final InterruptedException e) { // <-- possible error cases
      System.out.println("job was interrupted");
    } catch (final ExecutionException e) {
      System.out.println("caught exception: " + e.getCause());
    } catch (final TimeoutException e) {
      future.cancel(true); // <-- interrupt the job
      System.out.println("timeout");
    }
    // wait all unfinished tasks for 2 sec
    try {
      if (!executor.awaitTermination(15, TimeUnit.MINUTES)) {
        // force them to quit by interrupting
        executor.shutdownNow();
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }
  
}
