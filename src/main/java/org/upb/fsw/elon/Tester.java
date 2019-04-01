package org.upb.fsw.elon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.lang.reflect.Type;

import org.aksw.qa.commons.datastructure.Entity;
import org.assertj.core.util.Arrays;
import org.upb.fsw.elon.tools.DataSetMeasure;
import org.upb.fsw.elon.tools.Node;
import org.upb.fsw.elon.tools.StructureMeasure;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;

import org.upb.fsw.elon.EvaluationFlags;

public class Tester {

	private static String QUESTION_TYPE = "SBARQ";

	public static void main(String[] args) throws Exception {

//		String qfile = "/qald-9-train-questions.json";
//		List<String> questions = loadQ(qfile);
		Tree t = new Sentence("What is the birthplace of Albert Einstein?").parse();
		System.out.println(t.pennString());
//		SparqlQueryBuilder sparqlQueryBuilder = new SparqlQueryBuilder();
////		for (String q : questions) {
//			String q = "Which people were born in Heraklion?";
//			Tree t = new Sentence(q).parse();
//			System.out.println(q);
//			System.out.println(sparqlQueryBuilder.evalTree(t));
//			System.out.println("--------------------");
//		}
		
//
//		Dictionary dict = Dictionary.getDefaultResourceInstance();
//		MorphologicalProcessor mp = dict.getMorphologicalProcessor();
//		
//		
//		IndexWord mw = mp.lookupBaseForm(POS.VERB, "born in");

//		System.out.println(mw);
//		mw.getSenses().stream().forEach(s -> {
//			System.out.println(s);
//		});
	
		
		
		
//		System.out.println(iw.getSenses().get(0).getKey());
		
//		IndexWord word = dict.getIndexWord(POS.VERB, "like");
//		System.out.println(PointerUtils.getSynonyms(word.getSenses().get(0)));
//		IndexWord word = dict.lookupIndexWord(POS.NOUN, "leader");
		
//		String wd = "developed";
//		POS.getAllPOS().forEach(pos -> {
//			try {
//				IndexWord iwd = dict.lookupIndexWord(pos, wd);
//				System.out.println(pos + ": " + iwd);
//				if(iwd != null)
//					System.out.println(iwd.getSenses());
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		System.out.println(dict.getIndexWord(POS.NOUN, lemma));
		
		
//		System.out.println(word.getSenses());
//		System.out.println(word.getSenses());
//		PointerTargetTree hypernym = PointerUtils.getHypernymTree(word.getSenses().get(0));
//		PointerTargetNodeList synonyms = PointerUtils.getSynonyms(word.getSenses().get(0));
//		PointerTargetTree hyponym = PointerUtils.getHyponymTree(word.getSenses().get(0));
//		System.out.println(word.getLemma());
//		printTree(hyponym.getRootNode(), 0);
//		System.out.println("synonyms");
//		System.out.println(synonyms);
//		printTree(hypernym.getRootNode(), 0);

//		Tree t = new Sentence("What is the birthplace of Einstein?").parse();
//		System.out.println(t.pennString());
//		StructureMeasure.removeLeafs(t);
//		System.out.println(t.pennString());
//		
//		List<Tree> st = StructureMeasure.getSubTrees(t);
//		for(Tree tree : st) {
//			System.out.println("-----");
//			System.out.println(tree.pennString());
//		}
//		
//		System.out.println("---------");
//		System.out.println(sparqlQueryBuilder.evalTree(t));
//		

//		
//		String qstring = "select distinct ?uri WHERE {dbr:Angela_Merkel dbo:birthPlace ?uri}";
//		
//		System.out.println("Startin query...");
//		System.out.println(QueryController.doQueryAsList(qstring));
//		
	}

	private static void printTree(PointerTargetTreeNode node, int depth) {
		String word = "";
		for(Word w : node.getSynset().getWords())
			word += w.getLemma() + ", ";
		
		for(int i = 0; i < depth; i++)
			System.out.print("\t");
		System.out.println(depth + ":" + word);
		
		for(PointerTargetTreeNode child : node.getChildTreeList())
			printTree(child, depth+1);
		
		
	}

	private static List<String> loadQ(String filename) {
		try {
			String qald9train = new String(
					Files.readAllBytes(Paths.get(SparqlQueryBuilder.class.getResource(filename).toURI())),
					StandardCharsets.UTF_8);

			JsonArray q = (new JsonParser().parse(qald9train)).getAsJsonArray();

			Type t = new TypeToken<List<String>>() {
			}.getType();
			return new Gson().fromJson(q, t);

		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

//	private static void countLabels(Tree t, Map<String, Integer> count) {
//		for (Tree child : t.getChildrenAsList()) {
//			count.put(child.label().value(), count.get(key))
//		}
//	}

	private static Map<String, Map<String, Integer>> countLabels(Tree t) {
		Map<String, Map<String, Integer>> count = new HashMap<String, Map<String, Integer>>();
		for (Tree child : t.getChildrenAsList()) {
			Map<String, Integer> childmap = new HashMap<String, Integer>();
			count.put(child.label().value(), childmap);
//			countLabels(t, childmap);
		}
		return count;
	}
}
