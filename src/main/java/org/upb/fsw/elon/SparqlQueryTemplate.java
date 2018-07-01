package org.upb.fsw.elon;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SparqlQueryTemplate {
	private static String SELECT = "SELECT";
        private static String WHERE = "WHERE";
        private static String ORDER_BY_DESC = "ORDER BY DESC";
        private static String ORDER_BY_ASC = "ORDER BY ASC";
        private static String FILTER = "FILTER";
        
                
	private String query;
	
        public SparqlQueryTemplate() {
		this.query = "";
	}
	
                
	public SparqlQueryTemplate(String query) {
		this.query = query;
	}
	
	public String buildQueryString(List<String> parameters) {
		
		String finalQuery = query;
		
		for(String param: parameters) {
			int pos = finalQuery.indexOf("%s");
			String left = finalQuery.substring(0, pos + 2);
			String right = finalQuery.substring(pos+2, finalQuery.length());
			left = String.format(left, param);
			finalQuery = left + right;
		}
		
		return finalQuery;
	}
	public StringBuilder recoverTemplate(String questionWord) throws FileNotFoundException, IOException{
    StringBuilder emptyTemplate = new StringBuilder("");
    JSONParser parser = new JSONParser();
    
        try {
            FileReader fileReader = new FileReader("/Users/alejandromamchur/NetBeansProjects/Elon/templates.json");
            Object obj = parser.parse(fileReader);
            JSONObject jsonObject = (JSONObject)obj;
            
            JSONObject jsonTemplate = (JSONObject) jsonObject.get(questionWord);
            
            //Select
            String select = (String)jsonTemplate.get(SELECT);
            String selectFinal = SELECT+" "+select;
            emptyTemplate.append(selectFinal);
            
            //Where
            String whereFinal = " "+WHERE+" ";
            emptyTemplate.append(whereFinal);
            JSONArray jsonarray = (JSONArray) jsonTemplate.get(WHERE);
            for(Object i: jsonarray){
                String msg = (String) i;
                emptyTemplate.append(msg);
            }
            //Order by desc
            String orderByDesc = (String)jsonTemplate.get(ORDER_BY_DESC);
            
            if(orderByDesc != null){
                String orderByDescFinal = " "+ORDER_BY_DESC+" "+orderByDesc;
                emptyTemplate.append(orderByDescFinal);
            }
            
            
            //Order by asc
            String orderByAsc = (String)jsonTemplate.get(ORDER_BY_ASC);
            
            if(orderByAsc != null){
                String orderByAscFinal = " "+ORDER_BY_ASC+" "+orderByAsc;
                emptyTemplate.append(orderByAscFinal);
            }
            
            //Filter
            String filter = (String)jsonTemplate.get(FILTER);
            
            if(filter != null){
                String filterFinal = " "+FILTER+" "+filter;
                emptyTemplate.append(filterFinal);
            }
            
        } catch (ParseException ex) {
            Logger.getLogger(SparqlQueryTemplate.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    
    return emptyTemplate;
}

}
