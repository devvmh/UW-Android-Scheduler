package com.callysto.devin.skedulr.types;

import java.io.Serializable;

/**
 * Represent one section of a course offered at UW.
 * 
 */
@SuppressWarnings("serial")
public class Section implements Serializable {
	private String sec;
	private String type;
	private String room;
	private boolean[] days = new boolean[7];
	private String startTime;
	private String endTime;
	private String formattedTime;
	
	//id's to identify what section type this is
	public static final int INVALID = -1;
	public static final int LAB = 0;
	public static final int LEC = 1;
	public static final int TUT = 2;

	/**
	 * Construct the section from given information.
	 * 
	 * @param sec
	 *            Section number, eg. 001
	 * @param type
	 *            One of {"LEC", "TUT", "LAB"}
	 * @param room
	 *            The building and room where it's held. eg MC4020
	 * @param days
	 *            The days the section meets represented as a string of 0's
	 *            (doesn't meet) and 1's (does meet). The first day is Monday.
	 * @param startTime
	 *            The starting time of the section.
	 * @param endTime
	 *            The ending time of the section.
	 */
	public Section(String sec, String type, String room, String days,
			String startTime, String endTime) {
		this.sec = sec;
		this.type = type;
		this.room = room;
		this.startTime = startTime;
		this.endTime = endTime;

		assert (days.length() == 7);
		for (int i = 0; i < 7; i++) {
			this.days[i] = days.charAt(i) == '1';
		}

		this.formattedTime = this.formatTime();
	}

	public String getFormattedTime() {
		return this.formattedTime;
	}

	/** Get the section number. */
	public String getSec() {
		return sec;
	}

	/** Get the section type; one of {LEC, TUT, LAB}. */
	public String getType() {
		return type;
	}

	/** Get the building and room where the section meets. */
	public String getRoom() {
		return room;
	}

	/** Get the days when this section meets. */
	public boolean[] getDays() {
		return days;
	}

	/** Get this section's starting time. */
	public String getStartTime() {
		return startTime;
	}
	
	/** Get this section's ending time. */
	public String getEndTime() {
		return endTime;
	}
	
	public float getStartTimeNum() {
		float time = Integer.parseInt(startTime.substring(0,2));
		float minute = Integer.parseInt(startTime.substring(3,5));
		time += minute / 60d;
		return time;
	}
	
	public float getEndTimeNum() {
		float time = Integer.parseInt(endTime.substring(0,2));
		float minute = Integer.parseInt(endTime.substring(3,5));
		time += minute / 60d;
		return time;
	}
	
	public int getTypeId() {
		if (type.equals("LEC")) {
			return LEC;
		} else if (type.equals("LAB")) {
			return LAB;
		} else if (type.equals("TUT")) {
			return TUT;
		} else {
			return INVALID;
		}
	}

	// Meaningful abbreviations for the days of the week in toString.
	private static String[] dayOfWeek = { "M", "T", "W", "Th", "F", "Sa", "Su" };

	@Override
	/** Print a meaningful represenation of this section. */
	public String toString() {
		if (sec.equals("No sections match your filter")) {
			return sec;
		}//if
		
		StringBuilder sb = new StringBuilder();

		sb.append("   Section " + this.type + " " + this.sec + " ");
		for (int i = 0; i < this.days.length; i++) {
			if (this.days[i]) {
				sb.append(Section.dayOfWeek[i]);
			}
		}

		//trim the seconds from the times for cleanliness
		String startTime = this.startTime.substring(0, this.startTime.lastIndexOf(":"));
		String endTime = this.endTime.substring(0, this.endTime.lastIndexOf(":"));
		sb.append("\t" + startTime + "-" + endTime);
		sb.append(" " + this.room);
		return sb.toString();
	}

	private String formatTime() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(this.startTime.substring(0,
					this.startTime.lastIndexOf(':')));
			sb.append('-');
			sb.append(this.endTime.substring(0, this.endTime.lastIndexOf(':')));
			sb.append(' ');
			for (int i = 0; i < this.days.length; i++) {
				if (this.days[i])
					sb.append(dayOfWeek[i]);
			}
			return sb.toString();
		} catch (StringIndexOutOfBoundsException e) {
			System.err
					.println("StringIndexOutOfBounds in Section.formatTime.  start="
							+ this.startTime + "; end=" + this.endTime);
			return "";
		}
	}
	
	public String daysAsBitString() {
		String val = "";
		for (boolean b : days) {
			val += (b) ? "1" : "0";
		}
		return val;
	}

	public boolean meetsAt(int day, int hr, int min) {
		if (!this.days[day])
			return false;
		String time = String.format("%02d:%02d:00", new Integer(hr),
				new Integer(min));

		return this.startTime.compareTo(time) <= 0
				&& time.compareTo(this.endTime) <= 0;
	}

	public boolean conflicts(Section other) {
		boolean sameDay = false;
		for (int i = 0; i < 7; i += 1) {
			if (this.days[i] && other.days[i]) {
				sameDay = true;
				break;
			}//if
		}//for
		if (sameDay == false) return false;
		
		//time overlap?
		if (   (this.getStartTimeNum() < other.getEndTimeNum() 
				&& this.getEndTimeNum() > other.getStartTimeNum())
			|| (this.getEndTimeNum() > other.getStartTimeNum()
				&& this.getStartTimeNum() < other.getEndTimeNum())) {
			return true;
		}//if
		
		return false;
	}//conflicts
}//Section
