package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.qa.annotation.index.IndexDBO;
import org.aksw.qa.annotation.index.IndexDBO_classes;
import org.aksw.qa.annotation.index.IndexDBO_properties;
import org.aksw.qa.annotation.sparql.SimpleQuantityRanker;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;
import org.h2.engine.SysProperties;

import com.google.common.collect.Sets;

public class Elon {
	private static Elon elon = null;
	private static String modelURI = "http://dbpedia.org/sparql",
			queryHeader = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX  dct:  <http://purl.org/dc/terms/> prefix dbp: <http://dbpedia.org/property/>";
	private ASpotter spotter;
	private IndexDBO classes, properties;
	private SimpleQuantityRanker ranker;
	
	private Elon() {
		this.spotter = new Spotlight();
		this.classes = new IndexDBO_classes();
		this.properties = new IndexDBO_properties();
		this.ranker = new SimpleQuantityRanker();
	}
	
	public static Elon getInstance() {
		if(elon == null)
			elon = new Elon();
		
		return elon;
	}
	
	
	public void setSpotter(ASpotter spotter) {
		this.spotter = spotter;
	}
	
	public ASpotter getSpotter() {
		return this.spotter;
	}
	
	private void setClass(Entity e) {
		List<String> posClasses = new ArrayList<String>();
		for(Resource r : e.getPosTypesAndCategories())
			posClasses.addAll(classes.search(r.getLocalName().toLowerCase()));
		
		if(posClasses.size() == 1) {
			e.setType(posClasses.get(0));
			return;
		}
			
		
		String posClass = ranker.rank(posClasses);
		e.setType(posClass);
	}
	
	private List<Entity> cleanEntities(List<Entity> entities) {
		
		List<Entity> cleanedEntities = new ArrayList<Entity>();
		
		for(Entity e : entities) {
			if(!e.getPosTypesAndCategories().isEmpty())
				cleanedEntities.add(e);
		}
		return cleanedEntities;
	}
	
	private String getProperty(String question){
		String q = new String(question);
		List<List<String>> possibleProperties = new ArrayList<List<String>>();
		Set<String> propSet = new HashSet<String>();
		
		if(q.charAt(q.length()-1) == '?')
			q.substring(0, q.length()-1);
		
		
		for(String word : q.split(" ")) {
			List<String> currProps = properties.search(word);
			if(!currProps.isEmpty())
				possibleProperties.add(currProps);
		}
		
			
			
		for(List<String> props : possibleProperties)
			propSet.addAll(props);
		
		for(List<String> props : possibleProperties)
			propSet = Sets.intersection(propSet, new HashSet<String>(props));
		
		List<String> propsList = new ArrayList<String>(propSet);
		
		if(propsList.isEmpty())
			return null;
		
		if(propsList.size() == 1)
			return propsList.get(0);
		
		return ranker.rank(propsList);
	}
	
	private List<Entity> removeFalseEntities(List<Entity> entities) {
		List<Entity> cleanEntities = new ArrayList<Entity>();
		
		for(Entity e: entities) {
			try {
				setClass(e);
				cleanEntities.add(e);
			} catch (Exception ex) {}
		}
		
		
		return cleanEntities;
	}
	
	
	public String ask(String question) throws UnableToAnswerException {
		
		if(question.equals(""))
			throw new UnableToAnswerException("Your question is empty.");
		
		List<Entity> entities = this.spotter.getEntities(question).get("en");
		
		if(entities.isEmpty())
			throw new UnableToAnswerException("No entities found in the question.");
		
		entities = cleanEntities(entities);
		
		if(entities.isEmpty())
			throw new UnableToAnswerException("No entities with classes found in the question.");
		
		
		String property = getProperty(question);
		
		if(property == null)
			throw new UnableToAnswerException("No property found in the question.");
		
		
		entities = removeFalseEntities(entities);
			
		
			
		
		SparqlQueryTemplate phQuery = new SparqlQueryTemplate("select ?o where  { <%s> <%s> ?o } ");
		//TODO add proper templates
		List<String> props = new ArrayList<String>();
		props.add(entities.get(0).getUris().get(0).getURI());
		props.add(property);		
		System.out.println(entities);
		System.out.println(property);
		
		String phFinishedQuery = phQuery.buildQueryString(props);
		List<QuerySolution> res = ResultSetFormatter.toList(doQuery(phFinishedQuery));
		
		return formAnswer(property, entities.get(0).getLabel(), res.get(0));
	}
	
	private String formAnswer(String prop, String ent, QuerySolution qs) {
		String answer = "";
		answer += "The " + prop + " of " + ent + " is " + qs.get("o");
		
		return answer;
	}
		
	
	private ResultSet doQuery(String queryString) throws UnableToAnswerException{
		
			Query query = QueryFactory.create(queryString);
			
			if(!query.isSelectType())
				throw new UnableToAnswerException("Only Select queries can be answered.");
				
			QueryExecution exec = QueryExecutionFactory.sparqlService(modelURI, queryString);
			ResultSet res = exec.execSelect();
			
			return res;
	}

}
