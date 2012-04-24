package com.callysto.devin.skedulr.types;

public class CourseSection {
	public final Course course;
	public final Section section;

	public CourseSection(Course course, Section section) {
		this.course = course;
		this.section = section;
	}
	
	public String asString() {
		String val = course.getSubject();
		val += ";" + course.getCatalog();
		val += ";" + course.getTitle();
		val += ";" + section.getType();
		val += ";" + section.getSec();
		val += ";" + section.daysAsBitString();
		val += ";" + section.getStartTime();
		val += ";" + section.getEndTime();
		val += ";" + section.getRoom();
		return val;
	}
}
