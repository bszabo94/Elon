package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

public class SparqlQueryBuilder {

	public static void main(String[] args) {

		SparqlQueryBuilder sparqlbuilder = new SparqlQueryBuilder();
		String question = "What is the birthplace of Angela Merkel?";

		Sentence sentence = new Sentence(question);
		Tree tree = sentence.parse();
		Tree sbarq = getChildbyLabel(tree, "SBARQ");

		System.out.println(tree.pennString());
		System.out.println(tree.depth());
		System.out.println(sbarq.depth());
		

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
