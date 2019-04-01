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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.assertj.core.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

public class SparqlQueryBuilder {

	private static String PROPERTY_QUERY_TEMPLATE = "select distinct ?property where { %s ?property [] . }",
			PROPERTY_QUERY_TEMPLATE_REV = "select distinct ?property where { [] ?property %s . }";

	public ASpotter spotter;

	public static void main(String[] args) throws URISyntaxException, IOException {

		ASpotter spotter = new Spotlight();

		String qald9train = "";
//
//			for (Tree t : np) {
//				if (possibleEntity(t)) {
//					List<Entity> foundEntities = spotter.getEntities(treeToString(t, true)).get("en");
//					writer.write(treeToString(t, true) + "\n***********\n");
//					if (foundEntities == null || foundEntities.isEmpty())
//						continue;
//					for (Entity e : foundEntities) {
//						writer.write(e.getUris().get(0).getURI()+"\n");
//					}
//				}
//			}
////				writer.write("depth: " + t.depth() + "\n" + t.pennString() + "\n");
//		}
//
//		writer.close();

	}

	public SparqlQueryBuilder() {
		this.spotter = new Spotlight();
	}

	public String evalTree(Tree tree) throws JWNLException {

		switch (tree.label().value()) {
		case "ROOT":
			return evalTree(tree.getChild(0));
		case "SBARQ":
			return evalSBARQ(tree);

		}

		return null;
	}

	public void traverseTree(Tree tree) {
		System.out.println("lvl 0");
		System.out.println(tree);

		System.out.println("lvl 1");
		List<Tree> children1 = tree.getChildrenAsList().get(0).getChildrenAsList();
		System.out.println(children1);
		for (Tree t : children1)
			System.out.println(t + "\n-------");

	}

	public String evalSBARQ(Tree sbarq, EvaluationFlags... evaluationFlags) throws JWNLException {
		return evalSQ(getChildbyLabel(sbarq, "SQ"));
	}

	public String evalSQ(Tree sq, EvaluationFlags... evaluationFlags) throws JWNLException {
		if (sq.numChildren() == 1 && hasChildWithLabel(sq, "VP")) {
			return evalVP(getChildbyLabel(sq, "VP"), EvaluationFlags.OBJECTQUERY);
		} else if (hasChildWithLabel(sq, "VBD") && hasChildWithLabel(sq, "VP")) {
			return evalVP(getChildbyLabel(sq, "VP"), EvaluationFlags.SUBJECTQUERY);
		}
		return evalNP(getChildbyLabel(sq, "NP"));
	}

	public String evalVP(Tree vp, EvaluationFlags... evaluationFlags) throws JWNLException {
		if (hasChildWithLabel(vp, "NP")) {
			String entityURI = evalNP(getChildbyLabel(vp, "NP"));
			// TODO other than VBD

			String propertyURI = findProperty(entityURI, treeToString(getChildbyLabel(vp, "VBD"), false), POS.VERB);

			List<String> answer;
			if (Arrays.asList(evaluationFlags).contains(EvaluationFlags.OBJECTQUERY))
				answer = QueryController.findObject("<" + propertyURI + ">", "<" + entityURI + ">");
			else if (Arrays.asList(evaluationFlags).contains(EvaluationFlags.SUBJECTQUERY))
				answer = QueryController.findSubject("<" + propertyURI + ">", "<" + entityURI + ">");
			else // TODO
				answer = QueryController.findObject("<" + propertyURI + ">", "<" + entityURI + ">");

			String answers = "";
			for (String a : answer)
				answers += a + "\n";

			return answers;
		} else if (hasChildWithLabel(vp, "PP")) {
			String entityURI = evalPP(getChildbyLabel(vp, "PP"));
			// TODO other than VBD

			String propertyURI = findProperty(entityURI, treeToString(getChildbyLabel(vp, "VBN"), false), POS.VERB);

			List<String> answer;
			if (Arrays.asList(evaluationFlags).contains(EvaluationFlags.OBJECTQUERY))
				answer = QueryController.findObject("<" + propertyURI + ">", "<" + entityURI + ">");
			else if (Arrays.asList(evaluationFlags).contains(EvaluationFlags.SUBJECTQUERY))
				answer = QueryController.findSubject("<" + propertyURI + ">", "<" + entityURI + ">");
			else // TODO
				answer = QueryController.findObject("<" + propertyURI + ">", "<" + entityURI + ">");

			String answers = "";
			for (String a : answer)
				answers += a + "\n";

			return answers;
		}

		// TODO other than np
		return null;
	}

	public String evalPP(Tree pp, EvaluationFlags... evaluationFlags) throws JWNLException {
		return evalNP(getChildbyLabel(pp, "NP"));
	}

	public String evalNP(Tree np, EvaluationFlags... evaluationFlags) throws JWNLException {
		if (possibleEntity(np)) {
			List<Entity> foundEntities = this.spotter.getEntities(treeToString(np, true)).get("en");

			// TODO occurance for more than one possible entities
			Entity candidate = foundEntities.get(0);

			return candidate.getUris().get(0).getURI();

		} else if (hasChildWithLabel(np, "PP")) {
			String entityURI = evalPP(getChildbyLabel(np, "PP"));
			String propertyURI = findProperty(entityURI, treeToString(getChildbyLabel(np, "NP"), false), POS.NOUN);

			List<String> answer = QueryController.findObject("<" + propertyURI + ">", "<" + entityURI + ">");

			String answers = "";
			for (String a : answer)
				answers += a + "\n";

			return answers;

		} else {
			return null;
		}
	}

	public String findProperty(String entityURI, String property, POS pos) throws JWNLException {
		String properEntityURI = "<" + entityURI + ">";
		String queryString = String.format(PROPERTY_QUERY_TEMPLATE_REV, properEntityURI);
		List<RelationCandidate> propertyCandidates = new ArrayList<RelationCandidate>();
		Dictionary dict = Dictionary.getDefaultResourceInstance();

		for (String candidateURI : QueryController.doQueryAsList(queryString)) {
			RelationCandidate candidate = new RelationCandidate(candidateURI);
			try {
				candidate.calcScore(property, pos, dict);
			} catch (JWNLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			propertyCandidates.add(candidate);
		}
		;

		propertyCandidates.sort((c1, c2) -> {
			return c2.getScore() < c1.getScore() ? 1 : -1;
		});

		if (!propertyCandidates.isEmpty())
			return propertyCandidates.get(0).getURI();
		else
			return null;
	}

	public void sortRelations(List<String> relations, String base) {
		relations.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				LevenshteinDistance dist = LevenshteinDistance.getDefaultInstance();
				Integer d1 = dist.apply(removeNS(o1).toLowerCase(), base.toLowerCase());
				Integer d2 = dist.apply(removeNS(o2).toLowerCase(), base.toLowerCase());

				return d1 - d2;
			}

		});
	}

	public void processQuestion(String question) {
		Sentence sentence = new Sentence(question);
		Tree tree = sentence.parse();

//		traverseTree(tree);

//		System.out.println(tree.pennString());
//		Sentence sentence2 = new Sentence("What's")

//		System.out.println("----");
//		System.out.println(treeToString(tree, false));
	}

	public boolean possibleEntity(Tree t) {
		if (t.depth() > 2)
			return false;

		for (Tree child : t.getChildrenAsList())
			if (child.label().value().equals("NNP") || child.label().value().equals("NNPS")
					|| child.label().value().equals("NNS"))
				return true;

		return false;
	}

	public boolean hasChildWithLabel(Tree t, String label) {
		for (Tree child : t.getChildrenAsList())
			if (child.label().value().equals(label))
				return true;

		return false;
	}

	public Tree getChildbyLabel(Tree tree, String label) {
		for (Tree child : tree.getChildrenAsList())
			if (child.value().toUpperCase().equals(label.toUpperCase()))
				return child;

		return null;
	}

	public String removeNS(String fullname) {
		String reverse = new StringBuilder(fullname).reverse().toString();
		reverse = reverse.substring(0, reverse.indexOf('/'));

		return new StringBuilder(reverse).reverse().toString();
	}

	public Tree getNode(Tree tree, String value) {
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

	public List<Tree> getNodes(Tree tree, String value) {
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

	public List<Tree> getAllNodes(Tree tree, String value, boolean onlyLeafs) {
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

	public String treeToString(Tree tree, boolean enableDT) {
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
