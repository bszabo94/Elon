package org.upb.fsw.elon;

import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QALDController {
	
	@RequestMapping(value = "/", produces= {"application/json"})
	public String qaldResponse(@RequestParam("query") String query, @RequestParam("lang") String lang) throws UnableToAnswerException, ParseException {
		Elon elon = Elon.getInstance();
		QALDResponse res = elon.processQuestion(query, lang);
		return res.getJSON().toJSONString();
	}
}
