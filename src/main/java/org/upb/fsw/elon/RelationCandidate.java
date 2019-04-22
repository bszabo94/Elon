package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class RelationCandidate {
	private String uri, phrase;
	private List<Double> distances;

	private static char URI_SEPARATOR = '/';
	private static LevenshteinDistance levensteinDistance;

	{
		levensteinDistance = LevenshteinDistance.getDefaultInstance();
	}

	public RelationCandidate(String uri) {
		this.uri = uri;
		this.phrase = uri.substring(uri.lastIndexOf(URI_SEPARATOR) + 1, uri.length());
		this.distances = new ArrayList<Double>();
	}

	public static LevenshteinDistance getLevensteinDistance() {
		return levensteinDistance;
	}

	public String getPhrase() {
		return phrase;
	}

	public String getURI() {
		return this.uri;
	}

	public List<Double> getDistances() {
		return this.distances;
	}
}
