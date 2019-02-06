package org.upb.fsw.elon;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.FolderNotFoundException;

import org.aksw.qa.annotation.index.IndexDBO;
import org.aksw.qa.annotation.index.IndexDBO_classes;
import org.aksw.qa.annotation.index.IndexDBO_properties;
import org.aksw.qa.annotation.sparql.SimpleQuantityRanker;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.algebra.evaluation.function.string.LowerCase;

import com.google.common.collect.Sets;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.UniversalPOSMapper;
import fuzzydl.QsugenoIntegral;

/**
 * 
 * Basically the class Elon implements all the logic to do the task.
 *
 */
public class Elon {
	private static Elon elon = null;
	/**
	 * URI to the knowledge base
	 */
	private static String modelURI = "http://dbpedia.org/sparql",
			/**
			 * currently unused, since NLIWOD uses full namespaces
			 */
			queryHeader = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX  dct:  <http://purl.org/dc/terms/> prefix dbp: <http://dbpedia.org/property/>";
	/**
	 * Holds the last query generated, useful to write that to the results too
	 */
	private String lastQuery;
	//NLIWOD objects
	private ASpotter spotter;
	private IndexDBO classes, properties;
	private SimpleQuantityRanker ranker;
	//Querybuilder
	private SparqlQueryBuilder querybuilder;
	
	//singleton
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
	
	/**
	 * 
	 * Using NLIWOD tries to find the class of an entity.
	 */
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
	
	/**
	 * Removes entities without classes
	 * Currently unused, the logic became deprecated
	 */
	private List<Entity> cleanEntities(List<Entity> entities) {
		
		List<Entity> cleanedEntities = new ArrayList<Entity>();
		
		for(Entity e : entities) {
			if(!e.getPosTypesAndCategories().isEmpty())
				cleanedEntities.add(e);
		}
		return cleanedEntities;
	}
	
	/**
	 * Gets the properties found in a string
	 */
	private List<String> getProperty(String question){
		String q = new String(question);
		List<List<String>> possibleProperties = new ArrayList<List<String>>();
		Set<String> propSet = new HashSet<String>();
		
		//removes question mark if there is one
		if(q.charAt(q.length()-1) == '?')
			q.substring(0, q.length()-1);
		
		//iterates all over the words
		for(String word : q.split(" ")) {
			List<String> currProps = properties.search(word);
			if(!currProps.isEmpty())
				possibleProperties.add(currProps);
		}
		
			
		
		for(List<String> props : possibleProperties)
			propSet.addAll(props);
		
		//creates an intersection of the possible properties, if more than one found
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
	
	/**
	 * Set the class of all Entities in a List
	 */
	private List<Entity> addClassToEntities(List<Entity> entities){
		List<Entity> annotatedEntities = new ArrayList<Entity>(entities);
		for(Entity e: annotatedEntities) {
			try {
				setClass(e);
			} catch (Exception ex) {}
		}
		
		return annotatedEntities;
	}
	
	/**
	 * 
	 * Ranks a List of given properties in ascending or descending order according to the logic of SimpleQuantityRanker
	 */
//	private List<String> rankProperties(List<String> properties, boolean ascending){
//		List<String> rankedProperties = new ArrayList<String>(),
//				propsToRank = new ArrayList<String>(properties),
//				currentRound;
//		
//		SimpleQuantityRanker ranker = new SimpleQuantityRanker();
//		System.out.println("ranking in progress");
//		while(!propsToRank.isEmpty()) {
//			System.out.println("sorting: " + propsToRank);
//			String best = ranker.rank(propsToRank);
//			rankedProperties.add(best);
//			propsToRank.remove(best);
//		}
//		
//		if(ascending)
//			Collections.reverse(rankedProperties);
//
//		return rankedProperties;
//	}
	
	public void parseQuestions(List<IQuestion> iquestions, String lang) {
		
		for(IQuestion iquestion : iquestions) {
			String question = iquestion.getLanguageToQuestion().get(lang);
			Sentence sentence = new Sentence(question);
			Tree tree = sentence.parse();
			System.out.println("Parsed: " + question);
			System.out.println("Depth: " + tree.depth());
			System.out.println("Solution is: " + iquestion.getSparqlQuery());
			Tree q = tree.firstChild();
			if(!q.label().value().equals("SBARQ"))
				continue;
			
			for(Tree child : q.children()) {
				System.out.println("Label of current child: " + child.label().value());
				System.out.println("Value of current child: " + child.getLeaves());
			}
			
			System.out.println("getting WHNP : " + getNode(getNode(tree, "SQ"), "NP"));
			
			tree.pennPrint();
			System.out.println("-----------------");
		}		
		
	}
	
//	public void processQuestion(String question) throws JSONException {
//		
//		
//		
//		
//		
//	}
	
	public void processQuestions(List<IQuestion> iquestions, String lang) throws IOException {
		final String answeredFileName = "answered.txt",
				nonansweredFileName = "nonanswered.txt";
		
		BufferedWriter af = new BufferedWriter(new FileWriter(answeredFileName)), naf = new BufferedWriter(new FileWriter(nonansweredFileName));
		
		int total = iquestions.size(), answered = 0;
		for(IQuestion iq : iquestions) {
			String question = iq.getLanguageToQuestion().get(lang);
			System.out.println("Processing question " + iquestions.indexOf(iq) + " out of " + iquestions.size());
			System.out.println(question);
			try {
				QALDResponse response = processQuestion(question, lang);
				if(response.isAnswered()) {
					String query = response.getQuery() == null ? "" : response.getQuery();
					af.write(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
					af.write("Answered " + answered + ", id: " + iq.getId() + "\n");
					af.write("Question: " + question + "\n");
					af.write("Found entities: [ ");
					for(String entity : response.getFoundEntities())
						af.write(entity + "; ");
					af.write(" ] \n");
					af.write("Found relations: [ ");
					for(String relation : response.getFoundRelations())
						af.write(relation + "; ");
					af.write(" ] \n");
					af.write("Expected query: " + iq.getSparqlQuery() + "\n");
					af.write("Found Query: " + query + "\n");
					af.write("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
					answered++;
				} else {
					naf.write(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
					naf.write("Id: " + iq.getId() + "\n");
					naf.write("Question: " + question + "\n");
					naf.write("Found entities: [ ");
					for(String entity : response.getFoundEntities())
						naf.write(entity + "; ");
					naf.write(" ] \n");
					naf.write("Found relations: [ ");
					for(String relation : response.getFoundRelations())
						af.write(relation + "; ");
					naf.write(" ] \n");
					naf.write("Expected query: " + iq.getSparqlQuery() + "\n");
					naf.write("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
					naf.write("Expected query: " + iq.getSparqlQuery() + "\n");
					naf.write("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
				}
					
			} catch (Exception e) {
				continue;
			}
		}
		af.close();
		naf.close();
		System.out.println("Out of " + total + " questions " + answered + " were answered.");
	}
	
	public QALDResponse processQuestion(String question, String lang) throws ParseException, JSONException {
		QALDResponse qaldresponse = new QALDResponse();
		qaldresponse.addQuestion(question, lang);
		
		Sentence sentence = new Sentence(question);
		Tree tree = sentence.parse().firstChild();
//		tree.pennPrint();
		//TODO
		if(!tree.label().value().equals("SBARQ")) {
//			System.out.println("Not SBARQ question.");
			return qaldresponse;
		}
			
		Tree whtree = getNode(tree, "WHNP");
		Tree firstNP = getNode(tree, "NP");
		Tree pptree = getNode(firstNP, "PP");
		Tree NPEntity = getNode(pptree, "NP");
		Tree NPRelation = getNode(firstNP.getChild(0), "NP");
		
		if(NPEntity == null || NPRelation == null)
			return qaldresponse;
		
//		NPEntity.pennPrint();
//		NPRelation.pennPrint();

		ASpotter spotter = new Spotlight();
		
		List<Entity> foundE = spotter.getEntities(treeToString(NPEntity, true)).get(lang);
		
		if(foundE == null || foundE.isEmpty())
			return qaldresponse;
		
		
		
		String entityURI = foundE.get(0).getUris().get(0).toString();
		
//		System.out.println(entityURI);
		
		List<String> relations = findRelations(treeToString(NPRelation, false), entityURI);
		
		
		
		
//		this.lastQuery = new String();
//		ASpotter spotter = new Spotlight();
//		//getting entities from question
//		List<Entity> entities = spotter.getEntities(question).get("en");
//		qaldresponse.setFoundEntities(entities);

		
		
//		//getting properties from question
//		List<String> properties = getProperty(question);
//
//		if(properties.isEmpty() && entities.size() == 1) {
//			//throw new UnableToAnswerException("No property and only one entity found.");
//			return qaldresponse;
//		}
		
		//if there are no proerties found, but more than one entities, it is possible that one of the entities are a property
		//tries to make properties out of entities with no classes
//		if(properties.isEmpty()) {			
//			entities = addClassToEntities(entities);
//			List<Entity> entitiesCopy = new ArrayList<Entity>(entities);
//			for(Entity e: entities) {
//				if(entities.size() == 1)
//					break;
//				if(e.getType().equals("")) {
//					properties.add("http://dbpedia.org/ontology/" + e.getLabel());
//					entitiesCopy.remove(e);
//				}
//			}
//			entities = entitiesCopy;
//			if(properties.isEmpty()) {
//				//throw new UnableToAnswerException("No property found in the question.");
//				return qaldresponse;
//			}
//				
//		}			
		

//		if(properties.size() > 1)
//			properties = rankProperties(properties, false);
		
		SparqlQueryTemplate phQuery = this.querybuilder.selectTemplate(question);	
		
//		qaldresponse.setFoundRelations(properties);
		
		
		List<String> props = new ArrayList<String>();
//		props.add(entities.get(0).getUris().get(0).getURI());
		
		props.add(entityURI);
		
		//tries a query with all the ordered properties, until it gets a result for the query
		while(!relations.isEmpty()) {
//			String bestproperty = ranker.rank(properties);
			props.add(relations.get(0));
			String phFinishedQuery = phQuery.buildQueryString(props);
			//System.out.println(phFinishedQuery);
			this.lastQuery = phFinishedQuery;
			ResultSet res;
			ResultSet res2;
			try {
				res = ResultSetFactory.copyResults(doQuery(lastQuery));
				res2 = ResultSetFactory.copyResults(doQuery(lastQuery));
			} catch (Exception e) {
				return qaldresponse;
			}
			
			//if there is a result, we can take that
			if(ResultSetFormatter.toList(res2).size() != 0) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ResultSetFormatter.outputAsJSON(bos, res);
								
				JSONObject resultobject =  new JSONObject(new String(bos.toByteArray()));
				
				qaldresponse.addAnswer(null, this.lastQuery, resultobject);
				
				return qaldresponse;
			}
			
			String rel = relations.remove(0);
			props.remove(rel);
		}
		
		return qaldresponse;		
	}
	
	
//	public QALDResponse processQuestion(String question, String lang) throws ParseException, JSONException {
//		QALDResponse qaldresponse = new QALDResponse();
//		qaldresponse.addQuestion(question, lang);
//		
//		if(question.equals(""))
//			//throw new UnableToAnswerException("Your question is empty.");
//			return qaldresponse;
//		
//		this.lastQuery = new String();
//		ASpotter spotter = new Spotlight();
//		//getting entities from question
//		List<Entity> entities = spotter.getEntities(question).get("en");
//		
//		if(entities == null || entities.isEmpty()) {
//			//throw new UnableToAnswerException("No entities found in the question.");
//			return qaldresponse;
//		}
//
//		qaldresponse.setFoundEntities(entities);
//		
//		//getting properties from question
//		List<String> properties = getProperty(question);
//
//		if(properties.isEmpty() && entities.size() == 1) {
//			//throw new UnableToAnswerException("No property and only one entity found.");
//			return qaldresponse;
//		}
//		
//		//if there are no proerties found, but more than one entities, it is possible that one of the entities are a property
//		//tries to make properties out of entities with no classes
//		if(properties.isEmpty()) {			
//			entities = addClassToEntities(entities);
//			List<Entity> entitiesCopy = new ArrayList<Entity>(entities);
//			for(Entity e: entities) {
//				if(entities.size() == 1)
//					break;
//				if(e.getType().equals("")) {
//					properties.add("http://dbpedia.org/ontology/" + e.getLabel());
//					entitiesCopy.remove(e);
//				}
//			}
//			entities = entitiesCopy;
//			if(properties.isEmpty()) {
//				//throw new UnableToAnswerException("No property found in the question.");
//				return qaldresponse;
//			}
//				
//		}			
//		
//
////		if(properties.size() > 1)
////			properties = rankProperties(properties, false);
//		
//		SparqlQueryTemplate phQuery = this.querybuilder.selectTemplate(question);	
//		
//		qaldresponse.setFoundRelations(properties);
//		
//		
//		List<String> props = new ArrayList<String>();
//		props.add(entities.get(0).getUris().get(0).getURI());
//		
//		//tries a query with all the ordered properties, until it gets a result for the query
//		while(!properties.isEmpty()) {
//			String bestproperty = ranker.rank(properties);
//			props.add(bestproperty);
//			String phFinishedQuery = phQuery.buildQueryString(props);
//			//System.out.println(phFinishedQuery);
//			this.lastQuery = phFinishedQuery;
//			ResultSet res;
//			ResultSet res2;
//			try {
//				res = ResultSetFactory.copyResults(doQuery(lastQuery));
//				res2 = ResultSetFactory.copyResults(doQuery(lastQuery));
//			} catch (Exception e) {
//				return qaldresponse;
//			}
//			
//			//if there is a result, we can take that
//			if(ResultSetFormatter.toList(res2).size() != 0) {
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				ResultSetFormatter.outputAsJSON(bos, res);
//								
//				JSONObject resultobject =  new JSONObject(new String(bos.toByteArray()));
//				
//				qaldresponse.addAnswer(null, this.lastQuery, resultobject);
//				
//				return qaldresponse;
//			}
//			
//			properties.remove(bestproperty);
//			props.remove(bestproperty);
//		}
//		
//		return qaldresponse;		
//	}
//	
	public String getLastQuery() {
		return this.lastQuery;
	}
		
	//does a query againts a knowledge base. Only select queries are working now.
	public ResultSet doQuery(String queryString){
//			System.out.println("doing query with: " + queryString);
//			System.out.println(queryString == null);
			Query query = QueryFactory.create(queryString);
			
//			if(!query.isSelectType())
//				throw new UnableToAnswerException("Only Select queries can be answered.");
				
			QueryExecution exec = QueryExecutionFactory.sparqlService(modelURI, queryString);
			
			return exec.execSelect();
	}
	
	private List<String> findRelations(String relation, String entityURI){
		String baseQuery = "select ?rel where { <" + entityURI + "> ?rel [] }";
		List<String> foundRelations = new ArrayList<String>();
		
		ResultSet queryRes = doQuery(baseQuery);
		List<QuerySolution> res = ResultSetFormatter.toList(queryRes);
		
		for(QuerySolution qs : res)
			foundRelations.add(qs.get("rel").toString());
		
		sortRelations(foundRelations, relation);
		
		return foundRelations;
	}
	
	public void sortRelations(List<String> relations, String base) {
		relations.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				LevenshteinDistance dist = LevenshteinDistance.getDefaultInstance();
				Integer d1 = dist.apply(removeNS(o1).toLowerCase(), base.toLowerCase());
				Integer d2 = dist.apply(removeNS(o2).toLowerCase(), base.toLowerCase());
				
				return d1 - d2;
			}
			
		});
	}
	
	private String removeNS(String fullname) {
		String reverse = new StringBuilder(fullname).reverse().toString();
		reverse = reverse.substring(0, reverse.indexOf('/'));
		
		return new StringBuilder(reverse).reverse().toString();
	}
	
	private Tree getNode(Tree tree, String value) {
		if(tree == null)
			return null;
		
		if(tree.label().value().equals(value))
			return tree;
		for(Tree child : tree.getChildrenAsList()) {
			Tree posNode = getNode(child, value);
			if(posNode != null)
				return posNode;
		}
		return null;
	}
	
	private List<Tree> getAllNodes(Tree tree, String value, boolean onlyLeafs){
		List<Tree> nodes = new ArrayList<Tree>(), childNodes = new ArrayList<Tree>();
		
		
		for(Tree child : tree.getChildrenAsList()){
			childNodes.addAll(getAllNodes(child, value, onlyLeafs));
		}
		
		if(tree.label().value().equals(value)) {
			if(childNodes.isEmpty() || !onlyLeafs)
				nodes.add(tree);
		}
			
		nodes.addAll(childNodes);
		
		return nodes;
	}
	
	private String treeToString(Tree tree, boolean enableDT) {
		StringBuilder sb = new StringBuilder();
		
		for(Tree child : tree.getChildrenAsList()) {
			if(child.isLeaf()) {
				if(enableDT)
					sb.append(child.value().toString() + " ");
				else if (!tree.label().value().equals("DT")) {
					sb.append(child.value().toString() + " ");
				}
					
			}
				
			else
				sb.append(treeToString(child, enableDT) + " ");
		}
		
		return sb.toString().trim();
		
	}

}
