package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;
/*
 * This class is accountable for choosing an appropriate query pattern to the question
 */
public class SparqlQueryBuilder {
	private List<SparqlQueryTemplate> templates;
	
	private void loadTemplates(){		
		this.templates.add(new SparqlQueryTemplate("select ?uri where  { <%s> <%s> ?uri } "));
	}
	
	public SparqlQueryBuilder() {
		this.templates = new ArrayList<SparqlQueryTemplate>();
		loadTemplates();
	}
	
	/*
	 * Selects a template according to the question and logic
	 * Currently, it only returns the default template, implementation of additional logic is possible in the future.
	 */
	public SparqlQueryTemplate selectTemplate(String question) {
		return this.templates.get(0);
	}
	
	
}
