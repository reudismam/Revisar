package br.ufcg.spg.lsh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import info.debatty.java.lsh.LSHMinHash;

public class ScriptLSHMinHash {

  public static void main(String[] args) {
    // proportion of 0's in the vectors
    // if the vectors are dense (lots of 1's), the average jaccard similarity
    // will be very high (especially for large vectors), and LSH
    // won't be able to distinguish them
    // as a result, all vectors will be binned in the same bucket...
    double sparsity = 0.75;

    // Number of sets
    int count = 10000;

    // Size of vectors
    int n = 100;

    // LSH parameters
    // the number of stages is also sometimes called thge number of bands
    int stages = 2;

    // Attention: to get relevant results, the number of elements per bucket
    // should be at least 100
    int buckets = 10;

    // Let's generate some random sets
    boolean[][] vectors = new boolean[count][n];
    Random rand = new Random();

    for (int i = 0; i < count; i++) {
      for (int j = 0; j < n; j++) {
        vectors[i][j] = rand.nextDouble() > sparsity;
      }
    }

    // Create and configure LSH algorithm
    LSHMinHash lsh = new LSHMinHash(stages, buckets, n);

    int[][] counts = new int[stages][buckets];

    // Perform hashing
    for (boolean[] vector : vectors) {
      int[] hash = lsh.hash(vector);
      for (int i = 0; i < hash.length; i++) {
        counts[i][hash[i]]++;
      }
      print(vector);
      System.out.print(" : ");
      print(hash);
      System.out.print("\n");
    }

    System.out.println("Number of elements per bucket at each stage:");
    for (int i = 0; i < stages; i++) {
      print(counts[i]);
      System.out.print("\n");
    }
  }
  
  public static List<List<Integer>> Lsh(boolean [][] vectors) {
    // proportion of 0's in the vectors
	// if the vectors are dense (lots of 1's), the average jaccard similarity
	// will be very high (especially for large vectors), and LSH
	// won't be able to distinguish them
	// as a result, all vectors will be binned in the same bucket...
	// Size of vectors
	int n = vectors[0].length;
	// LSH parameters
    // the number of stages is also sometimes called thge number of bands
	int stages = 2;
	// Attention: to get relevant results, the number of elements per bucket
	// should be at least 100
	int buckets = 100;
	// Create and configure LSH algorithm
	LSHMinHash lsh = new LSHMinHash(stages, buckets, n);
	int[][] counts = new int[stages][buckets];
	int [][] hashs = new int [vectors.length][stages];
	// Perform hashing
	List<List<List<Integer>>> hashBuckets = new ArrayList<>();
	for (int i = 0; i < stages; i++) {
		hashBuckets.add(new ArrayList<>());
		for (int j = 0; j < n; j++) {
			hashBuckets.get(i).add(new ArrayList<>());
		}
	}
	for (int j = 0; j < vectors.length; j++) {
      boolean[] vector = vectors[j];
	  int[] hash = lsh.hash(vector);
	  hashs[j] = hash;
	  for (int i = 0; i < hash.length; i++) {
	    counts[i][hash[i]]++;
	    hashBuckets.get(i).get(hash[i]).add(j);
	  }
	  print(vector);
      System.out.print(" : ");
	  print(hash);
	  System.out.print("\n");
	}
	System.out.println("Number of elements per bucket at each stage:");
	for (int i = 0; i < stages; i++) {
	  print(counts[i]);
	  System.out.print("\n");
	}
	
	List<List<Integer>> clusters = new ArrayList<>();
	for (int i = 0; i < vectors.length; i++) {
		int hash []  = hashs[i];
		List<Integer> toCompare = new ArrayList<>();
		for (int j = 0; j < stages; j++) {
			List<Integer> inBuckets = hashBuckets.get(j).get(hash[j]);
			toCompare.addAll(inBuckets);
		}
		clusters.add(toCompare);
	}; 
	return clusters;
  }

  static void print(int[] array) {
    System.out.print("[");
    for (int v : array) {
      System.out.print("" + v + " ");
    }
    System.out.print("]");
  }

  static void print(boolean[] array) {
    System.out.print("[");
    for (boolean v : array) {
      System.out.print(v ? "1" : "0");
    }
    System.out.print("]");
  }
}