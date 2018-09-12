package org.upb.fsw.elon;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.json.JSONWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *Just a class to hold the main function for testing the Elon class
 */
@SpringBootApplication
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
		
		/*Elon elon = Elon.getInstance();
		//loads both the qald8 train and test questions
		List<IQuestion> testquestions = LoaderController.load(Dataset.QALD8_Test_Multilingual);
		List<IQuestion> trainquestions = LoaderController.load(Dataset.QALD8_Train_Multilingual);
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
		String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH_mm"));
		
		try {
			elon.ask(testquestions, "elon_test_" + date + "_" + time + ".json");
			elon.ask(trainquestions, "elon_train_" + date + "_" + time + ".json");
		}catch (Exception e) {
			e.printStackTrace();
		}*/
		
		
		/*Elon elon = Elon.getInstance();
		try {
			JSONObject question = new JSONObject();
			question.put("lang", "en");
			question.put("query", "What is the birthplace of Angela Merkel?");
			System.out.println(elon.processQuestion(question).getJSON().toJSONString());
		} catch (UnableToAnswerException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		/*int port = Integer.parseInt(args[0]);
		
		Elon elon = Elon.getInstance();
		System.out.println("Elon created.");
		Javalin app = Javalin.create().start(port);
		
		System.out.println("Server created.");
		app.exception(Exception.class, (e, ctx) -> {
			e.printStackTrace();
			ctx.result("got exception");
		});
		app.post("/", ctx -> {
			String query = ctx.formParam("query"),
					lang = ctx.formParam("lang");
			
			JSONObject queryobject = new JSONObject();
			queryobject.put("query", query);
			queryobject.put("lang", lang);
			
			try {
				QALDResponse resp = elon.processQuestion(queryobject);
				ctx.json(resp.getJSON());
			}catch (Exception e) {
				e.printStackTrace();
				ctx.result("got an exception in try");
			}
			

			
		});*/
	}
	
}
