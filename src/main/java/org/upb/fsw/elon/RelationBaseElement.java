package org.upb.fsw.elon;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;

public class RelationBaseElement {
	private Synset synset;
	private PointerTargetTreeNode hypernyms;
	private PointerTargetTreeNode hyponyms;

	public RelationBaseElement(Synset synset) throws JWNLException {
		this.synset = synset;
		this.hypernyms = PointerUtils.getHypernymTree(this.synset).getRootNode();
		this.hyponyms = PointerUtils.getHyponymTree(this.synset).getRootNode();
	}

	public Synset getSynset() {
		return synset;
	}

	public PointerTargetTreeNode getHypernyms() {
		return hypernyms;
	}

	public PointerTargetTreeNode getHyponyms() {
		return hyponyms;
	}

	public void calcDistances(RelationCandidate candidate) {
		String candidateLemma = candidate.getPhrase();

		// Check base element
		for (Word word : synset.getWords()) {
			String lemma = word.getLemma();
			if (lemma.equals(lemma.toLowerCase())) {
				double dist = distFunction(candidateLemma, lemma, 1);
				candidate.getDistances().add(dist);
			}
		}

		// Check trees
		calcDistInTree(candidate, this.hypernyms, 1);
		calcDistInTree(candidate, this.hyponyms, 1);

	}

	private void calcDistInTree(RelationCandidate candidate, PointerTargetTreeNode node, int depth) {
		String candidateLemma = candidate.getPhrase();
		for (Word word : node.getSynset().getWords()) {
			String lemma = word.getLemma();
			if (lemma.equals(lemma.toLowerCase())) {
				double dist = distFunction(candidateLemma, lemma, depth);
				candidate.getDistances().add(dist);
			}
		}

		for (PointerTargetTreeNode child : node.getChildTreeList())
			calcDistInTree(candidate, child, depth + 1);
	}

	private double distFunction(String candidateLemma, String baseLemma, int depth) {
		double basedist = RelationCandidate.getLevensteinDistance().apply(candidateLemma, baseLemma);
		double occurance = candidateLemma.indexOf(baseLemma) == -1 ? 3.0 : 0.2;

		return basedist * occurance * depth;
	}
}
