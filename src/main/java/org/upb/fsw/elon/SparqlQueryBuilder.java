package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;

public class SparqlQueryBuilder {
	private List<SparqlQueryTemplate> templates;
	
	private void loadTemplates(){		
		this.templates.add(new SparqlQueryTemplate("select ?uri where  { <%s> <%s> ?uri } "));
	}
	
	public SparqlQueryBuilder() {
		this.templates = new ArrayList<SparqlQueryTemplate>();
		loadTemplates();
	}
	
	public SparqlQueryTemplate selectTemplate(String question) {
		return this.templates.get(0);
	}
	
	
}
