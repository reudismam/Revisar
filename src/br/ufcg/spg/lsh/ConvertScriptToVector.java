package br.ufcg.spg.lsh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.Script;
import de.jail.geometry.schemas.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvertScriptToVector {

	private static final Logger logger = LogManager.getLogger(ConvertScriptToVector.class.getName());

	private ConvertScriptToVector(){
	}


	private static Map<String, Integer> getVocabulary(List<Point> points) {
		Map<Integer, String> vocabulary = new HashMap<>();
		Map<String, Integer> vocabularyInv = new HashMap<>();
		int featureId = 0;
		for (Point point : points) {
			@SuppressWarnings("unchecked")
			Script<StringNodeData> sc = (Script<StringNodeData>) point;
			for (EditNode<StringNodeData> data : sc.getList()) {
				String identity = data.identity();
				if (!vocabulary.containsValue(identity)) {
					vocabulary.put(featureId++, identity);
					vocabularyInv.put(identity, featureId - 1);
				}
			}
		}
		int id = 1;
		while (vocabulary.get(id) != null) {
			logger.trace("id: " + id + " value: " + vocabulary.get(id));
			id++;
		}
		return vocabularyInv;
	}
	
	public static boolean[][] vector(List<Point> points) {
		Map<String, Integer> vocabulary = getVocabulary(points);
		boolean[][] ds = new boolean[points.size()][vocabulary.size()];
		int i = 0;
		for (Point point : points) {
			boolean[] d = vector(point, vocabulary);
			ds[i++] = d;
		}
		return ds;
	}
	
	public static boolean [] vector(Point point, Map<String, Integer> vocabulary) {
		@SuppressWarnings("unchecked")
		Script<StringNodeData> sc = (Script<StringNodeData>) point;
		boolean[] d = new boolean[vocabulary.size()];
		for (EditNode<StringNodeData> data : sc.getList()) {
			String identity = data.identity();
			d[vocabulary.get(identity)] = true;
		}
		return d;
	}
}
