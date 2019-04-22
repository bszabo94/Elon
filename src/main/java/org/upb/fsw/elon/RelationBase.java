package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;

public class RelationBase {
	private String lemma;
	private POS pos;
	private List<RelationBaseElement> elements;

	public RelationBase(String lemma, POS pos, Dictionary dict) throws JWNLException {
		this.lemma = lemma;
		this.pos = pos;
		this.elements = new ArrayList<RelationBaseElement>();
		generateElements(dict);
	}

	private void generateElements(Dictionary dict) throws JWNLException {
		if (this.pos == POS.NOUN) {
			IndexWord iw;
			iw = dict.getIndexWord(pos, lemma);
			if (iw == null)
				iw = dict.lookupIndexWord(pos, lemma);
			if (iw == null)
				throw new JWNLException("Could not find IndexWord for " + lemma + ".");

			for (Synset synset : iw.getSenses())
				elements.add(new RelationBaseElement(synset));
		} else if (this.pos == POS.VERB) {
			MorphologicalProcessor mp = dict.getMorphologicalProcessor();
			Set<Synset> synsetset = new HashSet<Synset>();
			IndexWord mw = mp.lookupBaseForm(this.pos, lemma);

			for (Synset sense : mw.getSenses()) {
				for (Word word : sense.getWords()) {
					IndexWord iw = dict.getIndexWord(POS.NOUN, word.getLemma());
					for (Synset s : iw.getSenses())
						synsetset.add(s);
				}
			}

			for (Synset synset : synsetset)
				elements.add(new RelationBaseElement(synset));

		}

	}

	public void calcDistances(RelationCandidate candidate) {
		for (RelationBaseElement baseElement : elements)
			baseElement.calcDistances(candidate);
	}

}
