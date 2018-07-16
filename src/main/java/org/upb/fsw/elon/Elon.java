package org.upb.fsw.elon;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.Sets;


public class Elon {
	private static Elon elon = null;
	private static String modelURI = "http://dbpedia.org/sparql",
			queryHeader = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX  dct:  <http://purl.org/dc/terms/> prefix dbp: <http://dbpedia.org/property/>";
	private String lastQuery;
	private ASpotter spotter;
	private IndexDBO classes, properties;
	private SimpleQuantityRanker ranker;
	private SparqlQueryBuilder querybuilder;
	
	private Elon() {
		this.spotter = new Spotlight();
		this.classes = new IndexDBO_classes();
		this.properties = new IndexDBO_properties();
		this.ranker = new SimpleQuantityRanker();
		this.querybuilder = new SparqlQueryBuilder();
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
	
	private List<String> getProperty(String question){
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
		
		return propsList;
		
		/*if(propsList.isEmpty())
			return null;
		
		if(propsList.size() == 1)
			return propsList.get(0);
		
		return ranker.rank(propsList);*/
	}
	
	/*private List<Entity> removeFalseEntities(List<Entity> entities) {
		List<Entity> cleanEntities = new ArrayList<Entity>();
		
		for(Entity e: entities) {
			try {
				setClass(e);
				cleanEntities.add(e);
			} catch (Exception ex) {}
		}
		
		
		return cleanEntities;
	}*/
	
	private List<Entity> addClassToEntities(List<Entity> entities){
		List<Entity> annotatedEntities = new ArrayList<Entity>(entities);
		for(Entity e: annotatedEntities) {
			try {
				setClass(e);
			} catch (Exception ex) {}
		}
		
		return annotatedEntities;
	}
	
	private List<String> rankProperties(List<String> properties, boolean ascending){
		List<String> rankedProperties = new ArrayList<String>(),
				propsToRank = new ArrayList<String>(properties);
		
		SimpleQuantityRanker ranker = new SimpleQuantityRanker();
		
		while(!propsToRank.isEmpty()) {
			String best = ranker.rank(propsToRank);
			rankedProperties.add(best);
			propsToRank.remove(best);
		}
		
		if(ascending)
			Collections.reverse(rankedProperties);

		return rankedProperties;
	}
	
	public void ask(List<IQuestion> iquestions, String ofilename) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(ofilename)));
		boolean first = true;
		bw.write("{\"questions\": [");
		
		int counter = 0;
		for(IQuestion iq : iquestions) {
			String question = iq.getLanguageToQuestion().get("en");
			String answer;
			try {
				//System.out.println("DEBUG, Question " + iquestions.indexOf(iq) + " of " + iquestions.size() + " : " + question);
				answer = elon.ask(question);
			} catch (Exception e) {
				continue;
			}
			
			if(!first)
				bw.write(", ");
			
			bw.write(" { \"id\": \"" + iq.getId() + "\",");
			bw.write(" \"question\": [ { \"language\": \"en\", \"string\": \"" + iq.getLanguageToQuestion().get("en").replaceAll("\"", "'") + "\" } ],");
			bw.write(" \"query\": { \"sparql\": \"" + this.getLastQuery().replaceAll("\"", "'") + "\" },");
			bw.write("\"answers\": [ " + answer + "]");
			bw.write("}");
			bw.newLine();
			
			first = false;
			counter++;
		}
		
		bw.write("]}");
		bw.close();
		System.out.println(counter + " out of " + iquestions.size() + " questions has been answered.");
	}
	
	public String ask(String question) throws UnableToAnswerException {
		
		if(question.equals(""))
			throw new UnableToAnswerException("Your question is empty.");
		
		this.lastQuery = new String();
		ASpotter spotter = new Spotlight();
		List<Entity> entities = spotter.getEntities(question).get("en");
		
		if(entities == null || entities.isEmpty()) {
			throw new UnableToAnswerException("No entities found in the question.");
		}
				
		List<String> properties = getProperty(question);
		
		if(properties.isEmpty() && entities.size() == 1) {
			throw new UnableToAnswerException("No property and only one entity found.");
		}
		
		if(properties.isEmpty()) {			
			entities = addClassToEntities(entities);
			for(Entity e: entities) {
				if(entities.size() == 1)
					break;
				if(e.getType().equals("")) {
					properties.add("http://dbpedia.org/ontology/" + e.getLabel());
					entities.remove(e);
				}
			}
			
			if(properties.isEmpty()) {
				throw new UnableToAnswerException("No property found in the question.");
			}
				
		}			
		
		
		properties = rankProperties(properties, false);
		SparqlQueryTemplate phQuery = this.querybuilder.selectTemplate(question);	
		
		List<String> props = new ArrayList<String>();
		props.add(entities.get(0).getUris().get(0).getURI());
		
		for(String property : properties) {
			props.add(property);
			String phFinishedQuery = phQuery.buildQueryString(props);
			System.out.println(phFinishedQuery);
			this.lastQuery = phFinishedQuery;
			ResultSet res = ResultSetFactory.copyResults(doQuery(lastQuery));
			ResultSet res2 = ResultSetFactory.copyResults(doQuery(lastQuery));
			
			if(ResultSetFormatter.toList(res2).size() != 0) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ResultSetFormatter.outputAsJSON(bos, res);
				
				return new String(bos.toByteArray());
			}
			
			props.remove(property);
		}
		
		throw new UnableToAnswerException("No result for query.");		
	}
	

	
	public String getLastQuery() {
		return this.lastQuery;
	}
		
	
	private ResultSet doQuery(String queryString) throws UnableToAnswerException{
		
			Query query = QueryFactory.create(queryString);
			
			if(!query.isSelectType())
				throw new UnableToAnswerException("Only Select queries can be answered.");
				
			QueryExecution exec = QueryExecutionFactory.sparqlService(modelURI, queryString);
			
			return exec.execSelect();
	}

}
