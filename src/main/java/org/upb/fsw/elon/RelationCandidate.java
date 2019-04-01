package org.upb.fsw.elon;

import org.apache.commons.text.similarity.LevenshteinDistance;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;
import net.sf.extjwnl.dictionary.Dictionary;

public class RelationCandidate {
	private String uri, phrase;
	private double score;

	private static char URI_SEPARATOR = '/';
	private static LevenshteinDistance levensteinDistance = LevenshteinDistance.getDefaultInstance();

	public RelationCandidate(String uri) {
		this.uri = uri;
		this.phrase = uri.substring(uri.lastIndexOf(URI_SEPARATOR) + 1, uri.length());
		this.score = Double.MAX_VALUE;
	}

	public void calcScore(String targetLemma, POS pos, Dictionary dict) throws JWNLException {
		IndexWord word = dict.getIndexWord(pos, this.phrase);

		if (word == null)
			word = dict.lookupIndexWord(pos, this.phrase);

		if (word == null)
			return;

		for (Synset sense : word.getSenses()) {
			calcScoreFromTree(targetLemma, PointerUtils.getHypernymTree(sense).getRootNode(), 1);
			calcScoreFromTree(targetLemma, PointerUtils.getHyponymTree(sense).getRootNode(), 1);
		}
	}

	private void calcScoreFromTree(String targetLemma, PointerTargetTreeNode node, int distance) {
		for (Word word : node.getSynset().getWords()) {
			String candidateLemma = word.getLemma();
			if (candidateLemma.equals(candidateLemma.toLowerCase())) {
				double base = levensteinDistance.apply(candidateLemma, targetLemma);
				double candidateScore = Math.pow(base, distance);
				if (candidateScore < this.score)
					this.score = candidateScore;
			}
		}

		for (PointerTargetTreeNode child : node.getChildTreeList())
			calcScoreFromTree(targetLemma, child, distance + 1);
	}

	public String getURI() {
		return this.uri;
	}

	public double getScore() {
		return this.score;
	}
}
