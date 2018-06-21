package org.upb.fsw.elon;
import java.io.*;
import org.openrdf.query.algebra.Str;

public class QuestionWordCheck {
	
	public static String SearchQuestionWord(String str) {
		String [] questionWords = { "who", "where", "why", "what", "when", "how" };
		String [] arrayOfWords = str.split(" ");  
		
		for (int i = 0; i < arrayOfWords.length; i++) {
			for (int j = 0; j < questionWords.length + 1; j++) {
				if (arrayOfWords[i].equalsIgnoreCase(questionWords[j])) {
					return questionWords[j];
				}
			}
		}
		return "-1";
	}
	
	public static void main(String args) {
		SearchQuestionWord(args);
	}
}
