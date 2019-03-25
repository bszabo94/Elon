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
			SIMPLE_QUERY_TEMPLATE_OBJECT = "select distinct ?uri where { %s %s ?uri .}",
			SIMPLE_QUERY_TEMPLATE_SUBJECT = "select distinct ?uri where { ?uri %s %s .}",
			SIMPLE_QUERY_TEMPLATE_RELATION = "select distinct ?uri where { %s ?uri %s .}";

	public static List<String> findObject(String propertyURI, String subjectURI) {
		String queryString = buildQueryString(SIMPLE_QUERY_TEMPLATE_OBJECT, propertyURI, subjectURI);
		return doQueryAsList(queryString);
	}

	public static List<String> findSubject(String propertyURI, String objectURI) {
		String queryString = buildQueryString(SIMPLE_QUERY_TEMPLATE_SUBJECT, propertyURI, objectURI);
		return doQueryAsList(queryString);
	}

	public static List<String> findRelation(String propertyURI, String relationURI) {
		String queryString = buildQueryString(SIMPLE_QUERY_TEMPLATE_RELATION, propertyURI, relationURI);
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

	private static String buildQueryString(String template, String... parameters) {

		List<String> finalQueryParts = new ArrayList<String>();
		String finalQuery = template;
		String right;

		for (String param : parameters) {
			int pos = finalQuery.indexOf("%s");
			String left = finalQuery.substring(0, pos + 2);
			finalQuery = finalQuery.substring(pos + 2, finalQuery.length());
			left = String.format(left, param);
			finalQueryParts.add(left);
		}

		right = finalQuery;
		finalQuery = new String();
		for (String part : finalQueryParts)
			finalQuery += part;
		finalQuery += right;

		return finalQuery;
	}

}
