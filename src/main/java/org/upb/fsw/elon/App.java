package org.upb.fsw.elon;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Fox;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class App {

	public static void main(String[] args) {
		
		Set<RDFNode> systemAnswers = new HashSet();
		
		systemAnswers.add(new ResourceImpl("true"));
		
		for (ASpotter m : new ASpotter[] { new Spotlight() }) {
			Map<String, List<Entity>> ents = m.getEntities("where is germany");
			if (!ents.isEmpty()) {
				System.out.println("Succes. Found " + ents.size() + " entities.");
				
				for (Entity ent : ents.get("en")) {
					System.out.println(ent.toString());
				}
			} else {
				System.out.println("No entities found.");
			}
		}
		
	}

}
