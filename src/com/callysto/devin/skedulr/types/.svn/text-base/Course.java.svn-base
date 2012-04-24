package com.callysto.devin.skedulr.types;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.callysto.devin.skedulr.util.CourseParserCallBacks;

/**
 * Information about a Course offered.
 *
 */
@SuppressWarnings("serial")
public class Course implements Serializable {
	private String subject;
	private String catalog;
	private String title;
	private Section[] sections;
	
	/** Construct a course from provided data.
	 * 
	 * @param sub  Subject (e.g. "CS")
	 * @param cat  Catalog number (e.g. "349" or "129R")
	 * @param ttl  Title (e.g. "User Interfaces")
	 * @param sections	An array of sections offered for this course.
	 */
	public Course(String sub, String cat, String ttl, Section[] sections) {
		this.subject = sub;
		this.catalog = cat;
		this.title = ttl;
		this.sections = sections;
	}

	/** Get this course's subject. */
	public String getSubject() {
		return subject;
	}

	/** Get this course's catalog number. */
	public String getCatalog() {
		return catalog;
	}

	/** Get this course's title. */
	public String getTitle() {
		return title;
	}
	
	/** Get this course's sections. */
	public Section[] getSections() {
		// Exposing private data.  Bad! Bad!!
		return this.sections;
	}
	
	/** Return a string representing this course. */
	public String toString() {
		return this.subject + " " + this.catalog + " " + this.title;
	}
	
	/**
	 * Read the list of courses from the web service and parse it 
	 * into a list of Course objects.
	 * @return
	 */
	public static List<Course> coursesFactory() {
		System.err.println("Entering Course.coursesFactory()");
		List<Course> lst = new LinkedList<Course>();
		
		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			//parse the input and also register a private class for call backs
			sp.parse("http://anlujo.cs.uwaterloo.ca/cs349/courseList.py", new CourseParserCallBacks(lst));
			System.err.println("Course.coursesFactory: finished parsing");
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}

		
		return lst;
	}//coursesFactory
}//Course


/** 
 * A class providing the callbacks necessary to parse the list of courses.
 * @author bwbecker
 *
 */
/*
class CourseParserCallBacks extends DefaultHandler {
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
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("course")) {
			this.sections = new LinkedList<Section>();
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.tmpVal = new String(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("course")) {
			this.lst.add(new Course(this.subject, this.catalog, this.title, this.sections.toArray(new Section[0])));
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
	}
}*/
