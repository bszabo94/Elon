package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;

public class SparqlQueryTemplate {
	
	private String query;
	
	public SparqlQueryTemplate(String query) {
		this.query = query;
	}
	
	public String buildQueryString(List<String> parameters) {
		
		List<String> finalQueryParts = new ArrayList<String>();
		String finalQuery = this.query;
		String right;
		
		for(String param:parameters) {
			int pos = finalQuery.indexOf("%s");
			String left = finalQuery.substring(0, pos + 2);
			finalQuery = finalQuery.substring(pos+2, finalQuery.length());
			left = String.format(left, param);
			finalQueryParts.add(left);
		}
		
		right = finalQuery;
		finalQuery = new String();
		for(String part: finalQueryParts)
			finalQuery += part;
		finalQuery += right;
		
		return finalQuery;
	}
}
