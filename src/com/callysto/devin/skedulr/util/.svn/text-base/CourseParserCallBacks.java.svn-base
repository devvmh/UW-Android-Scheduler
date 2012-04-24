package com.callysto.devin.skedulr.util;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.Section;


/** 
 * A class providing the callbacks necessary to parse the list of courses.
 * @author bwbecker
 *
 */
public class CourseParserCallBacks extends DefaultHandler {
	private List<Course> lst;
	
	private String tmpVal;
	
	// values to add to the course currently being parsed.
	private String subject;
	private String catalog;
	private String title;
	private String sec;
	private String type;
	private String room;
	private String days;
	private String startTime;
	private String endTime;
	private List<Section> sections;
	
	
	public CourseParserCallBacks(List<Course> lst) {
		this.lst = lst;
		this.tmpVal = "";
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("course")) {
			this.sections = new LinkedList<Section>();
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.tmpVal += new String(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		this.tmpVal = this.tmpVal.trim();
		if (qName.equalsIgnoreCase("course")) {
			Course c = new Course(this.subject, this.catalog, this.title, this.sections.toArray(new Section[0]));
			this.lst.add(c);
		} else if (qName.equalsIgnoreCase("section")) {
			this.sections.add(new Section(this.sec, this.type, this.room, this.days, this.startTime, this.endTime));
		} else if (qName.equalsIgnoreCase("subject")) {
			this.subject = this.tmpVal;
		} else if (qName.equalsIgnoreCase("catalog")) {
			this.catalog = this.tmpVal;
		} else if (qName.equalsIgnoreCase("title")) {
			this.title = this.tmpVal;
		} else if (qName.equalsIgnoreCase("sec")) {
			this.sec = this.tmpVal;
		} else if (qName.equalsIgnoreCase("type")) {
			this.type = this.tmpVal;
		} else if (qName.equalsIgnoreCase("room")) {
			this.room = this.tmpVal;
		} else if (qName.equalsIgnoreCase("meet_days")) {
			this.days = this.tmpVal;
		} else if (qName.equalsIgnoreCase("meet_start_time")) {
			this.startTime = this.tmpVal;
		} else if (qName.equalsIgnoreCase("meet_end_time")) {
			this.endTime = this.tmpVal;
		}
		this.tmpVal = "";
	}
}//CourseParserCallbacks