package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;

public class SparqlQueryBuilder {
	private List<SparqlQueryTemplate> templates;
	
	private List<SparqlQueryTemplate> createTemplates(){
		List<SparqlQueryTemplate> templates = new ArrayList<SparqlQueryTemplate>();
		
		return templates;
	}
	
	public SparqlQueryBuilder() {
		this.templates = createTemplates(); //TODO
	}
	
	public SparqlQueryTemplate getTemplates(Question q) {
		//TODO
		return this.templates.get(0);
	}

}
