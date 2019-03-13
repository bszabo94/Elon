package org.upb.fsw.elon.tools;

import java.util.HashMap;
import java.util.Map;

public class Node {

	public Map<String, Node> children;
	public int countInParent;
	public Node parent;
	public String label;
	public int depth;

	public Node(String label, Node parent) {
		this.children = new HashMap<String, Node>();
		this.label = label;
		this.countInParent = 1;
		this.parent = parent;
		if (this.parent == null)
			this.depth = 0;
		else
			this.depth = this.parent.depth + 1;
	}

	public void print() {
		for (int i = 0; i < this.depth; i++)
			System.out.print("\t");

		System.out.println("(" + this.label + " " + this.countInParent + ")");

		for (Node child : this.children.values())
			child.print();
	}

	public void printGraphViz() {
		if (this.parent == null)
			System.out.println("digraph G {");

		for (Node child : this.children.values()) {

			String from;
			String to;

			if (this.parent != null)
				from = "\"" + this.depth + " " + this.parent.label + "->" + this.label + " " + this.countInParent
						+ "\"";
			else
				from = "\"" + this.depth + " " + this.label + "\"";

			to = "\"" + child.depth + " " + this.label + "->" + child.label + " " + child.countInParent + "\"";

			System.out.println(from + " -> " + to);
			child.printGraphViz();
		}

		if (this.parent == null)
			System.out.println("}");
	}

	public Node getMostFrequentChild() {
		int max = -1;
		Node best = null;

		for (Node child : this.children.values())
			if (child.countInParent > max) {
				max = child.countInParent;
				best = child;
			}

		return best;
	}

}
