package org.upb.fsw.elon.tools;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

public class StructureMeasure {

	public static List<Tree> getSubTrees(Tree t) {
		List<Tree> subtrees = new ArrayList<Tree>();

		subtrees.add(t);

		for (Tree child : t.getChildrenAsList())
//			if (!child.isLeaf())
			subtrees.addAll(getSubTrees(child));

		return subtrees;
	}

	public static boolean identical(Tree t1, Tree t2) {
		if (t1.getChildrenAsList().size() != t2.getChildrenAsList().size())
			return false;

		if (!t1.label().value().equals(t2.label().value()))
			return false;

		for (int i = 0; i < t1.getChildrenAsList().size(); i++)
			if (!identical(t1.getChild(i), t2.getChild(i)))
				return false;

		return true;
	}

	public static void removeLeafs(Tree t) {
		for (int i = 0; i < t.children().length; i++) {
			Tree child = t.getChild(i);
			if (child.isLeaf()) {
				t.removeChild(i);
				continue;
			}
			removeLeafs(child);
		}
	}
}
