package org.upb.fsw.elon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

public class SparqlQueryBuilder {

	private ASpotter spotter;

	public SparqlQueryBuilder() {
		this.spotter = new Spotlight();
	}

	public static void main(String[] args) throws URISyntaxException, IOException {

		ASpotter spotter = new Spotlight();

//		URL url = SparqlQueryBuilder.class.getResource("/qald-9-train-multilingual.json");

//		System.out.println(url);
//		File file = new File(url);
		String qald9train = "";
//		System.out.println("File read");
//		System.out.println(file);

		BufferedWriter writer = Files.newBufferedWriter(Paths.get("NPentities.txt"));
		try {
			qald9train = new String(
					Files.readAllBytes(
							Paths.get(SparqlQueryBuilder.class.getResource("/qald-9-train-multilingual.json").toURI())),
					StandardCharsets.UTF_8);

		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonParser parser = new JsonParser();
		JsonObject jo = parser.parse(qald9train).getAsJsonObject();
		JsonArray questions = jo.getAsJsonArray("questions");
		for (int i = 0; i < 20; i++) {

//		}
//		for (JsonElement q : questions) {
			JsonElement q = questions.get(i);
			String question = "";
			for (JsonElement innerq : q.getAsJsonObject().getAsJsonArray("question")) {
				if (innerq.getAsJsonObject().getAsJsonPrimitive("language").getAsString().equals("en")) {
					question = innerq.getAsJsonObject().getAsJsonPrimitive("string").getAsString();
					break;
				}
			}

			Sentence sentence = new Sentence(question);
			Tree tree = sentence.parse();
			if (!tree.getChild(0).label().value().equals("SBARQ"))
				continue;

			List<Tree> np = getNodes(tree, "NP");

			writer.write("-----------------------\n");
			writer.write(question + "\n");
			writer.write(tree.pennString());
			writer.write("__________________\n");

			for (Tree t : np) {
				if (possibleEntity(t)) {
					List<Entity> foundEntities = spotter.getEntities(treeToString(t, true)).get("en");
					writer.write(treeToString(t, true) + "\n***********\n");
					if (foundEntities == null || foundEntities.isEmpty())
						continue;
					for (Entity e : foundEntities) {
						writer.write(e.getUris().get(0).getURI()+"\n");
					}
				}
			}
//				writer.write("depth: " + t.depth() + "\n" + t.pennString() + "\n");
		}

		writer.close();

//		String question = "What is the birthplace of Angela Merkel?";
//
//		Sentence sentence = new Sentence(question);
//		Tree tree = sentence.parse();
//		Tree sbarq = getChildbyLabel(tree, "SBARQ");
//
//		System.out.println(tree.pennString());
//		System.out.println(tree.depth());
//		System.out.println(sbarq.depth());

//		SparqlQueryBuilder.processQuestion(question);

	}

	public static void traverseTree(Tree tree) {
		System.out.println("lvl 0");
		System.out.println(tree);

		System.out.println("lvl 1");
		List<Tree> children1 = tree.getChildrenAsList().get(0).getChildrenAsList();
		System.out.println(children1);
		for (Tree t : children1)
			System.out.println(t + "\n-------");

	}

	private static String evalSBARQ(Tree sbarq) {
		return evalSQ(getChildbyLabel(sbarq, "SQ"));
	}

	private static String evalSQ(Tree sq) {
		return evalNP(getChildbyLabel(sq, "NP"));
	}

	private static String evalNP(Tree np) {
		if (possibleEntity(np)) {

		}

		return null;
	}

	public static void processQuestion(String question) {
		Sentence sentence = new Sentence(question);
		Tree tree = sentence.parse();

//		traverseTree(tree);

//		System.out.println(tree.pennString());
//		Sentence sentence2 = new Sentence("What's")

//		System.out.println("----");
//		System.out.println(treeToString(tree, false));
	}

	private static boolean possibleEntity(Tree t) {
		if (t.depth() > 2)
			return false;

		for (Tree child : t.getChildrenAsList())
			if (child.label().value().equals("NNP") || child.label().value().equals("NNPS"))
				return true;

		return false;
	}

	private static Tree getChildbyLabel(Tree tree, String label) {
		for (Tree child : tree.getChildrenAsList())
			if (child.value().toUpperCase().equals(label.toUpperCase()))
				return child;

		return null;
	}

	private String removeNS(String fullname) {
		String reverse = new StringBuilder(fullname).reverse().toString();
		reverse = reverse.substring(0, reverse.indexOf('/'));

		return new StringBuilder(reverse).reverse().toString();
	}

	private static Tree getNode(Tree tree, String value) {
		if (tree == null)
			return null;

		if (tree.label().value().equals(value))
			return tree;
		for (Tree child : tree.getChildrenAsList()) {
			Tree posNode = getNode(child, value);
			if (posNode != null)
				return posNode;
		}
		return null;
	}

	private static List<Tree> getNodes(Tree tree, String value) {
		List<Tree> nodes = new ArrayList<Tree>();
		if (tree == null)
			return nodes;

		if (tree.label().value().equals(value))
			nodes.add(tree);

		for (Tree child : tree.getChildrenAsList()) {
			nodes.addAll(getNodes(child, value));
		}

		return nodes;
	}

	private static List<Tree> getAllNodes(Tree tree, String value, boolean onlyLeafs) {
		List<Tree> nodes = new ArrayList<Tree>(), childNodes = new ArrayList<Tree>();

		for (Tree child : tree.getChildrenAsList()) {
			childNodes.addAll(getAllNodes(child, value, onlyLeafs));
		}

		if (tree.label().value().equals(value)) {
			if (childNodes.isEmpty() || !onlyLeafs)
				nodes.add(tree);
		}

		nodes.addAll(childNodes);

		return nodes;
	}

	private static String treeToString(Tree tree, boolean enableDT) {
		StringBuilder sb = new StringBuilder();

		for (Tree child : tree.getChildrenAsList()) {
			if (child.isLeaf()) {
				if (enableDT)
					sb.append(child.value().toString() + " ");
				else if (!tree.label().value().equals("DT")) {
					sb.append(child.value().toString() + " ");
				}

			}

			else
				sb.append(treeToString(child, enableDT) + " ");
		}

		return sb.toString().trim();

	}

}
