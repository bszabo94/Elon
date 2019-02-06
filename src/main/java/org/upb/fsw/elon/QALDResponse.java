package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.List;

import org.aksw.qa.commons.datastructure.Entity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QALDResponse {
	private JSONObject mainObject;
	private boolean answered;
	private List<String> foundRelations, foundEntities;
	
	
	public QALDResponse() throws JSONException {
		this.foundEntities = new ArrayList<String>();
		this.foundRelations = new ArrayList<String>();
		this.answered = false;
		this.mainObject = new JSONObject();
		mainObject.put("questions", new JSONArray());
	}
	
	public String addQuestion(String query, String lang) throws JSONException {
		JSONArray ja = (JSONArray) this.mainObject.get("questions");
		
		JSONObject questionobject = new JSONObject();
		ja.put(questionobject);
		
		String id = Integer.toString(ja.length());
		questionobject.put("id", id);
		
		JSONArray questionarray = new JSONArray();
		questionobject.put("question", questionarray);
		
		JSONObject qobject = new JSONObject();
		questionarray.put(qobject);
		
		qobject.put("language", lang);
		qobject.put("string", query);
		questionobject.put("query", new JSONObject());
		questionobject.put("answers", new JSONArray());
		
		return id;
	}
	
	public void addAnswer(String id, String sparqlQuery, JSONObject answer) throws JSONException {
		JSONObject question = id == null ? getLastQuestion() : getQuestion(id);
		question.getJSONObject("query").put("sparql", sparqlQuery);
		question.getJSONArray("answers").put(answer);
		this.answered = true;
	}
	
	public JSONObject getQuestion(String id) throws JSONException {
		JSONArray questions = mainObject.getJSONArray("questions");
		for(int i = 0; i < questions.length(); i++) {
			JSONObject question = questions.getJSONObject(i);
			if (question.getString("id").equals(id))
				return question;
		}
		return null;
	}
	
	public String getQuery() {
		JSONObject question;
		try {
			question = this.getLastQuestion();
		} catch (JSONException e) {
			return null;
		}
		if(question == null)
			return null;
		
		String query;
		try {
			query = question.getJSONObject("query").getString("sparql");
		} catch (JSONException e) {
			return null;
		}
		
		return query;
	}
	
	public JSONObject getLastQuestion() throws JSONException {
		JSONArray ja = mainObject.getJSONArray("questions");
		
		if(ja.length() > 0)			
			return ja.getJSONObject(ja.length()-1);
		return null;
	}
	
	public JSONObject getMainObject() {
		return this.mainObject;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	public List<String> getFoundRelations() {
		return foundRelations;
	}

	public void setFoundRelations(List<String> foundRelations) {
		this.foundRelations = new ArrayList<String>(foundRelations);
	}

	public List<String> getFoundEntities() {
		return foundEntities;
	}

	public void setFoundEntities(List<Entity> foundEntities) {
		this.foundEntities = new ArrayList<String>();
		for(Entity entity: foundEntities)
			this.foundEntities.add(entity.getLabel());
		
	}
}
