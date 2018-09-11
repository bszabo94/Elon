package org.upb.fsw.elon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class QALDResponse {
	private JSONObject jsonobject;
	public QALDResponse() {
		this.jsonobject = new JSONObject();
		jsonobject.put("questions", new JSONArray());
	}
	
	public JSONObject addQuestion(String query, String lang) {
		JSONArray ja = (JSONArray) this.jsonobject.get("questions");
		int id = ja.size() + 1;
		JSONObject questionobject = new JSONObject();
		ja.add(questionobject);
		
		questionobject.put("id", id);
		JSONArray questionarray = new JSONArray();
		questionobject.put("question", questionarray);
		JSONObject qobject = new JSONObject();
		questionarray.add(qobject);
		
		qobject.put("language", lang);
		qobject.put("string", query);
		
		return questionobject;
	}
	
	public JSONObject getJSON() {
		return this.jsonobject;
	}
	
}
