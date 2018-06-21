who question:

SELECT ?person 
WHERE { ?person dbr: did/is ?property.
		?person dbr: something ?property2
	  }
	
	
when:

SELECT ?thingThatHappened 
WHERE ?thingThatHappened dbr: whichThing ?property .


which (biggest, tallest, highest)

SELECT ?thing  
WHERE { ?thing dbr: adjective ?property .
		?place dbr: noun ? property2}
ORDER BY DESC(?property)


which(smallest, shortest, lowest)

SELECT ?thing  
WHERE { ?thing dbr: adjective ?property .
		?place dbr: noun ? property2}
ORDER BY ASC(?property)


how many

SELECT DISTINCT(COUNT(?book)) 
WHERE { ?book dbr:something ?property . }


how many (more than)

SELECT DISTINCT(COUNT(?book)) 
WHERE { ?book dbr:something ?property . }
FILTER(?property > value)


how many (less than)

SELECT DISTINCT(COUNT(?book)) 
WHERE { ?book dbr:something ?property . }
FILTER(?property < value)


where question

SELECT ?somewhere
WHERE ?something dbr: thingAsked ?property
