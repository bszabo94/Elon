package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class QueryController {

	private static String PREFIX_STRING = "prefix dbo: <http://dbpedia.org/ontology/> prefix dbr: <http://dbpedia.org/resource/> ",
			SIMPLE_QUERY_TEMPLATE = "select distinct ?uri where { %s %s ?uri .}";

	public static List<String> findPropertyofEntity(String propertyURI, String entityURI){
		String queryString = String.format(SIMPLE_QUERY_TEMPLATE, entityURI, propertyURI);
		return doQueryAsList(queryString);
	}
	
	public static List<String> doQueryAsList(String queryString) {
		Query query = QueryFactory.create(PREFIX_STRING + queryString);

		QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		ResultSet rs = exec.execSelect();

		List<String> results = new ArrayList<String>();
		List<String> resultVars = rs.getResultVars();

		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			for (String var : resultVars)
				results.add(sol.get(var).toString());

		}

		return results;
//		ResultSetFormatter.outputAsJSON(exec.execSelect());

//		return rs.toString();
	}

	public static JsonObject doQueryAsJSON(String queryString) {
		Query query = QueryFactory.create(PREFIX_STRING + queryString);

		QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		ResultSet rs = exec.execSelect();

		ResultSetFormatter.outputAsJSON(rs);
		JsonElement resJson = new JsonParser().parse(ResultSetFormatter.asText(rs));

		return resJson.getAsJsonObject();
	}
}
