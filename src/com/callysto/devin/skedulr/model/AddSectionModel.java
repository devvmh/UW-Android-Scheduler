package com.callysto.devin.skedulr.model;

import java.util.Vector;

import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.Section;

/*
 * This class is the model for a single instance of the AddSectionActivity class.
 * It handles the filtering of individual sections.
 */
public class AddSectionModel extends BaseModel {
	private static MainModel mainModel = MainModel.getInstance();
	private static AddSectionModel instance = new AddSectionModel();
	private static Course currentCourse;
	private static Vector<Section> filteredSections;
	
	//display these section types or not?
	private boolean lab;
	private boolean lec;
	private boolean tut;
	
	private AddSectionModel() {
		filteredSections = new Vector<Section>();
		lab = true;
		lec = true;
		tut = true;
	}
	
	public static void setCurrentCourse(Course c) {
		currentCourse = c;
	}
	
	public static Course getCurrentCourse() {
		return currentCourse;
	}
	
	public static AddSectionModel getInstance() {
		return instance;
	}
	
	public void setIsLabEnabled(boolean val) {
		lab = val;
		redoFilteredList();
	}
	
	public void setIsLecEnabled(boolean val) {
		lec = val;
		redoFilteredList();
	}
	
	public void setIsTutEnabled(boolean val) {
		tut = val;
		redoFilteredList();
	}
	
	public boolean isLabEnabled() {
		return lab;
	}
	
	public boolean isLecEnabled() {
		return lec;
	}
	
	public boolean isTutEnabled() {
		return tut;
	}
	
	private boolean sectionTypeWorks(Section s) {
		switch(s.getTypeId()) {
		case Section.LAB:
			return lab;
		case Section.LEC:
			return lec;
		case Section.TUT:
			return tut;
		default:
			System.err.println("Invalid section type when building sections list");
			return true;
		}
	}
	
	private boolean timeWorks(Section s) {
		float filterStartTimeNum = (float) mainModel.getStartTimeHour() + 
				((float) mainModel.getStartTimeMinute()) / 60;
		float filterEndTimeNum = (float) mainModel.getEndTimeHour() + 
				((float) mainModel.getEndTimeMinute()) / 60;
		
		if (s.getStartTimeNum() >= filterStartTimeNum
				&& s.getEndTimeNum() <= filterEndTimeNum) {
			return true;
		} else {
			return false;
		}
	}//timeWorks
	
	public void redoFilteredList() {
		filteredSections.clear();
		for (Section s : currentCourse.getSections()) {
			if (sectionTypeWorks(s) && timeWorks(s)) {
				filteredSections.add(s);
			}
		}
		updateAllViews();
	}
	
	public Vector<Section> getFilteredSections() {
		return filteredSections;
	}

}
