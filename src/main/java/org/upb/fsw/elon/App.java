package org.upb.fsw.elon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.annotation.index.IndexDBO_classes;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Fox;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class App {

	public static void main(String[] args) throws UnableToAnswerException {
		
		
		Set<RDFNode> systemAnswers = new HashSet();
		
		systemAnswers.add(new ResourceImpl("true"));
		String question = "What is the population of Germany?";
		for (ASpotter m : new ASpotter[] { new Spotlight() }) {
			Map<String, List<Entity>> ents = m.getEntities("What is the population of Germany?");
			if (!ents.isEmpty()) {
				System.out.println("Succes. Found " + ents.get("en").size() + " entities."+ ents.get("en"));
				System.out.println("question word is: " + QuestionWordCheck.SearchQuestionWord(question));
				
				/*for (Entity ent : ents.get("en")) {
					System.out.println(ent.toString());
				}*/
				
				Entity e = ents.get("en").get(0);
				
				IndexDBO_classes classes = new IndexDBO_classes();

				
				for( Resource r : e.getPosTypesAndCategories()) {
					System.out.println("----");
					System.out.println(r.getLocalName());
					List<String> foundClasses = classes.search(r.getLocalName().toLowerCase());
					System.out.println("Possible classes:");
					for(String s : foundClasses) {
						System.out.println(s);
					}
				}
				
				
				
			} else {
				System.out.println("No entities found.");
			}
		}	
	}
}
