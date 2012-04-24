package com.callysto.devin.skedulr.util;

import java.util.List;
import java.util.Vector;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.CourseSection;
import com.callysto.devin.skedulr.types.Section;


/** 
 * A class providing the callbacks necessary to parse past
 * course selection data.
 * @author Devin Howard
 */
public class PastSelectionParserCallBacks extends DefaultHandler {
	private Vector<CourseSection> choices;
	private List<Course> courses;
	
	private String tmpVal;
	
	// values to add to the course currently being parsed.
	private String subject;
	private String catalog;
	private String section;
	
	public PastSelectionParserCallBacks(Vector<CourseSection> choices, List<Course> courses) {
		this.choices = choices;
		this.courses = courses;
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.tmpVal = new String(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("course")) {
			Course c = getCourse(this.subject, this.catalog);
			Section s = getSection(c, this.section);
			CourseSection cs = new CourseSection(c, s);
			if (s != null ) {
				this.choices.add(cs);
			}//if
		} else if (qName.equalsIgnoreCase("subject")) {
			this.subject = this.tmpVal;
		} else if (qName.equalsIgnoreCase("catalog")) {
			this.catalog = this.tmpVal;
		} else if (qName.equalsIgnoreCase("section")) {
			this.section = this.tmpVal;
		}//if
	}//endElement
	
	private Course getCourse(String subject, String catalog) {
		//continually iterate further in if the label, catalog #, and section number are the same
		for (Course c : this.courses) {
			if (subject.equals (c.getSubject())
					&& catalog.equals(c.getCatalog())) {
				return c;
			}//if
		}//for
		return null;
	}//getCourse
	
	private Section getSection(Course c, String section) {
		for (Section s : c.getSections()) {
			if (section.equals(s.getSec())) {
				return s;
			}//if
		}//for
		return null;
	}//getSection
}//PastSelectionParserCallBacks