package org.upb.fsw.elon;

import java.util.List;

public class SparqlQueryTemplate {
	
	private String query;
	
	public SparqlQueryTemplate(String query) {
		this.query = query;
	}
	
	public String buildQueryString(List<String> parameters) {
		
		String finalQuery = query;
		
		for(String param: parameters) {
			int pos = finalQuery.indexOf("%s");
			String left = finalQuery.substring(0, pos + 2);
			String right = finalQuery.substring(pos+2, finalQuery.length());
			left = String.format(left, param);
			finalQuery = left + right;
		}
		
		return finalQuery;
	}
	

}
