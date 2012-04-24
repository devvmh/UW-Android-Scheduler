package com.callysto.devin.skedulr.model;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callysto.devin.skedulr.R;
import com.callysto.devin.skedulr.types.Account;
import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.CourseSection;
import com.callysto.devin.skedulr.types.Section;
import com.callysto.devin.skedulr.util.ServerInteraction;

/*
 * Model for account management and storing the list of filtered courses
 * 6 sections:
 * 	fields + constructor are at the top
 * 	(I) Getters, setters, and convenience functions
 * 	(II) Course management functions
 * 	(III) Course filtering functions
 * 	(IV) Local storage functions
 * 	(V) Logging in and logging out - server interaction functions
 */

public class MainModel extends BaseModel{
	
	private static MainModel instance = new MainModel();
	
	private List<Course> courses;
	private List<Course> filteredCourses;
	private Vector<CourseSection> choices;
	private Vector<CourseSection> shortList;
	
	private Account acct;
	public boolean choicesNotLoadedYet = true;
	
	//list of which section types are included
	//index 0: LEC
	//index 1: LAB
	//index 2: TUT
	private boolean [] typeFilters;
	private String filterString;
	public Editable filterEditable;
	private int startTimeHour;
	private int startTimeMinute;
	private int endTimeHour;
	private int endTimeMinute;

	private MainModel() {
		super();
		acct = null; // if null, means you aren't logged in
		courses = new LinkedList<Course>();
		filteredCourses = new LinkedList<Course>();
		choices = new Vector<CourseSection>();
		shortList = new Vector<CourseSection>();
		typeFilters = new boolean[3];
			typeFilters[0] = true;
			typeFilters[1] = true;
			typeFilters[2] = true;
		filterString = "";
			
		updateAllViews();
	}//Model

/*
 * (I) Getters, setters, and convenience functions
 */
	
	
	public static MainModel getInstance() {
		return instance;
	}//getInstance
	
	public boolean areCoursesLoadedYet() {
		return courses != null;
	}//areCoursesLoadedYet
	
	public boolean doesItConflict(CourseSection newCS) {
		if (choices.size() >= 6) {
			return true;
		}
		Section newSec = newCS.section;
		for (CourseSection oldCS : choices) {
			Section oldSec = oldCS.section;
			if (oldSec.conflicts(newSec)) {
				return true;
			}
		}
		return false;
	}//doesItConflict

	public boolean isLoggedIn() {
		return acct != null;
	}//isLoggedIn
	
	public List<Course> getCourses() {
		return courses;
	}//getCourses
	
	public void setCourses(List<Course> val) {
		courses = val;
		updateAllViews();
	}//setCourses
	
	public List<Course> getFilteredCourses() {
		return filteredCourses;
	}//getFilteredCourses
	
	public void setFilterString(String val) {
		filterString = val;
	}//setFilterString
	
	public String getFilterString() {
		return filterString;
	}//getFilterString
	
	public void setStartTime(int hour, int minute) {
		startTimeHour = hour;
		startTimeMinute = minute;
		updateAllViews();
	}	
	public void setEndTime(int hour, int minute) {
		endTimeHour = hour;
		endTimeMinute = minute;
		updateAllViews();
	}
	
	public int getStartTimeHour() {
		return startTimeHour;
	}
	
	public int getStartTimeMinute() {
		return startTimeMinute;
	}
	
	public int getEndTimeHour() {
		return endTimeHour;
	}
	
	public int getEndTimeMinute() {
		return endTimeMinute;
	}
	
	public String getTimeString(int hour, int minute) {
		String AMPM;
		String h;
		if (hour > 12) {
			AMPM = "PM";
			h = String.valueOf(hour - 12);
		} else {
			AMPM = "AM";
			h = String.valueOf(hour);
			if (hour == 0) h = "12"; //exception!
		}
		
		String m = String.valueOf(minute);
		if (minute < 10) {
			m = "0" + m;
		}
		return h + ":" + m + " " + AMPM;
	}
	
	public String getStartTimeString() {
		return getTimeString(startTimeHour, startTimeMinute);
	}
	
	public String getEndTimeString() {
		return getTimeString(endTimeHour, endTimeMinute);
	}
	
	public Vector<CourseSection> getChoices() {
		return choices;
	}//getChoices
	
/*
 * (II) Course management functions
 */
	
	public void addToShortList(CourseSection cs) {
		shortList.add(cs);
	}
	
	public boolean addChoice(CourseSection cs) {
		if (doesItConflict(cs)) {
			return false;
		}
		choices.add(cs);
		updateAllViews();
		return true;
	}//addChoices
	
	public void removeChoice(CourseSection cs) {
		choices.remove(cs);
		updateAllViews();
	}//removeChoice
	
	public Course getCourseFromSearch(Context context) {
		try {
			StringTokenizer st = new StringTokenizer(this.filterString);
			//right now assumes we have two tokens, one is course subject and one is course catalog
			String subject = st.nextToken().toUpperCase();
			String catalog = st.nextToken();
			for (Course c : courses) {
				if (subject.equals(c.getSubject()) && catalog.equals(c.getCatalog())) {
					//need a mechanism for adding sections
					return c;
				}
			}//for
		} catch (NoSuchElementException e) {
			return null;
		}

		Toast.makeText(context, context.getResources().getString(R.string.course_not_found), Toast.LENGTH_SHORT).show();
		return null;
	}//addCourseFromString
	
/*
 * (III) Course filtering functions
 */
	
	public void redoFilteredList() {
		//copy
		filteredCourses = new LinkedList<Course>(courses);
		
		removeByCourseCode();
		removeByTime();
		updateAllViews();
	}//updateFilteredList
	
	public void shortenFilteredList() {
		//faster updater for the filter for live search update
		//unlike redoFilteredList, only removes
		removeByCourseCode();
		updateAllViews();
	}//shortenFilteredList
	
	private void removeByTime() {
		Vector<Course> toRemove = new Vector<Course>();
		float filterStartTimeNum = (float) startTimeHour + ((float) startTimeMinute) / 60;
		float filterEndTimeNum = (float) endTimeHour + ((float) endTimeMinute) / 60;
		for (Course c : filteredCourses) {
			//checking if ANY sections are OK. If any are, leave this course in the list.
			boolean noProperSections = true;
			for (Section s : c.getSections()) {
				if (s.getStartTimeNum() > filterStartTimeNum
						&& s.getEndTimeNum() < filterEndTimeNum) {
					noProperSections = false;
					break;
				}
			}
			if (noProperSections) {
				toRemove.add(c);
			}
		}
		for (Course c : toRemove) {
			filteredCourses.remove(c);
		}//for
	}//removeByTime
	
	private void removeByCourseCode() {
		Vector<Course> toRemove = new Vector<Course>();
		String filter = filterString.toUpperCase();
		for (Course c : filteredCourses) {
			if (! c.toString().startsWith(filter, 0)) {
				toRemove.add(c);
			}//if
		}//for
		for (Course c : toRemove) {
			filteredCourses.remove(c);
		}//for
	}//removeByCourseCode

/*
 * (IV) Local storage functions
 */
	
	public void saveFilters(SharedPreferences.Editor editor) {
		editor.putBoolean("filters_LEC", typeFilters[0]);
		editor.putBoolean("filters_LAB", typeFilters[1]);
		editor.putBoolean("filters_TUT", typeFilters[2]);
		editor.putString("filters_string", filterString);
		editor.putInt("filters_start_time_hour", startTimeHour);
		editor.putInt("filters_start_time_minute", startTimeMinute);
		editor.putInt("filters_end_time_hour", endTimeHour);
		editor.putInt("filters_end_time_minute", endTimeMinute);
	}//saveFilters
	
	public void loadFilters(SharedPreferences savedData) {
		typeFilters[0] = savedData.getBoolean("filters_LEC", true);
		typeFilters[1] = savedData.getBoolean("filters_LAB", true);
		typeFilters[2] = savedData.getBoolean("filters_TUT", true);
		filterString = savedData.getString("filters_string", "");
		startTimeHour = savedData.getInt("filters_start_time_hour", 8);
		startTimeMinute = savedData.getInt("filters_start_time_minute", 30);
		endTimeHour = savedData.getInt("filters_end_time_hour", 21);
		endTimeMinute = savedData.getInt("filters_end_time_minute", 00);
	}//loadFilters
	
	//local storage of choices
	public void saveChoices(SharedPreferences.Editor editor) {
		int i = 0;
		
		// remove old choices
		while (i < 6) {
			String key = "choice" + String.valueOf(i);
			editor.remove(key);
			i += 1;
		}// while
		
		for (i = 0; i < choices.size(); i += 1) {
			CourseSection cs = choices.get(i);
			String key = "choice" + String.valueOf(i);
			String value = cs.asString();
			
			editor.putString(key, value);
		}// for
	}// saveChoices
	
	//local storage of choices
	public void loadChoices(SharedPreferences savedData) {
		if (! isLoggedIn()) {
			return;
		}//if
		
		for (int i = 0; i < 6; i += 1) {
			String key = "choice" + String.valueOf(i);
			String courseSectionString = savedData.getString(key, "no course found");
			if (courseSectionString.equals("no course found")) {
				break; //ran out of courses
			}//if
			
			StringTokenizer st = new StringTokenizer (courseSectionString, ";");
			
			String subject = st.nextToken();
			String catalog = st.nextToken();
			String title = st.nextToken();
			Course course = new Course(subject, catalog, title, null);
			//doesn't matter if this is null since we never directly
			//compare course objects, just section data and course name info
			
			String type = st.nextToken();
			String sec = st.nextToken();
			String days = st.nextToken();
			String start = st.nextToken();
			String end = st.nextToken();
			String room = st.nextToken();
			Section section = new Section(sec, type, room, days, start, end);
			
			CourseSection cs = new CourseSection(course, section);
			choices.add(cs);
			choicesNotLoadedYet = false;
		}//for
		updateAllViews();
	}//loadChoices
	
	public void saveAccount(SharedPreferences.Editor editor) {
		if (acct == null) {
			editor.putBoolean("acct_isnull", true);
		} else {
			editor.putBoolean("acct_isnull", false);
			editor.putString("acct_userid", this.acct.userid);
			editor.putString("acct_password", this.acct.password);
			editor.putString("acct_givenNames", this.acct.givenNames);
			editor.putString("acct_surname", this.acct.surname);
		}
	}//saveAccount
	
	public void loadAccount(SharedPreferences savedData, Context context) {
		boolean isnull = savedData.getBoolean("acct_isnull", true);
		String userid = savedData.getString("acct_userid", null);
		String passwd = savedData.getString("acct_password", null);
		String given = savedData.getString("acct_givenNames", null);
		String surname = savedData.getString("acct_surname", null);
		if (isnull || userid == null || passwd == null || given == null || surname == null) {
			logout(savedData.edit());
			return;
		}
		try {
			//false in the means that we're not creating the account with the server, just loading
			Account a = new Account(userid, passwd, given, surname, false);
			
			//true here means that selections should exist on the server
			login(a, true, context);
		} catch (Exception e) {
			//the exceptions don't happen if creatingAccount parameter is false so we're good
		}//try-catch
	}//loadAccount
	
/*
 * (V) Logging in and logging out - server interaction functions
 */
	//convenience method - accesses account's userid and password for you
	public void saveCoursesAsync(Context context, ProgressBar pb) {
		ServerInteraction.saveCoursesAsync(acct.userid, acct.password, choices, context, pb);
	}
	
	//convenience method - accesses account's userid and password for you
	public boolean saveCourses(Context context, ProgressBar pb) {
		return ServerInteraction.saveCourses(acct.userid, acct.password, choices, context, pb);
	}
	
	public boolean login(Account a, boolean selectionsExistOnServer, Context context) {
		if (selectionsExistOnServer) {
			try {
				ServerInteraction.getCourseSelections(a.userid, a.password, context);
			} catch (Exception e) {
				return false;
			}//try-catch
		}//if
		acct = a;
		updateAllViews();
		return true;
	}//login
	
	//removes choices from memory
	public void logout(SharedPreferences.Editor editor) {
		this.acct = null;
		
		// remove old choices
		int i = 0;
		while (i < 6) {
			String key = "choice" + String.valueOf(i);
			editor.remove(key);
			i += 1;
		}// while
		
		choices.clear();
		choicesNotLoadedYet = true;
		updateAllViews();
	}//logout
}//Model
