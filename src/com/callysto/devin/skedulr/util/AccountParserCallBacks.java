package com.callysto.devin.skedulr.util;

import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * A class providing the callbacks necessary to parse an account's data.
 * @author Devin Howard
 */

public class AccountParserCallBacks extends DefaultHandler {
	private List<String> lst;
	
	private String tmpVal;
	
	// values to add to the course currently being parsed.
	private String givenNames;
	private String surname;

	public AccountParserCallBacks(List<String> lst) {
		this.lst = lst;
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.tmpVal = new String(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("account")) {
			this.lst.add(this.givenNames);
			this.lst.add(this.surname);
		} else if (qName.equalsIgnoreCase("surname")) {
			this.surname = this.tmpVal;
		} else if (qName.equalsIgnoreCase("given_names")) {
			this.givenNames = this.tmpVal;
		}//if
	}//endElement
}//AccountParserCallBacks