package org.upb.fsw.elon.tools;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

public class DataSetMeasure {

	public static void main(String[] args) {
		Node root = new Node("ROOT", null);

		Tree t = new Sentence("What is the birthplace of Angela Merkel?").parse();

		measureTree(t, root);

		t.pennPrint();
//		root.print();
		root.printGraphViz();

	}

	public static void measureTree(Tree t, Node currnode) {
		for (Tree child : t.getChildrenAsList()) {
			if (!child.isLeaf()) {
				String label = child.label().value();
				if (currnode.children.containsKey(label)) {
					currnode.children.get(label).countInParent += 1;
				} else {
					currnode.children.put(label, new Node(label, currnode));
				}
				measureTree(child, currnode.children.get(label));
			}
		}
	}

}
