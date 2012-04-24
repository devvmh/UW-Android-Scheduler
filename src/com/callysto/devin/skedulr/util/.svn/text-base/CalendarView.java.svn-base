package com.callysto.devin.skedulr.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.callysto.devin.skedulr.model.MainModel;
import com.callysto.devin.skedulr.types.CourseSection;
import com.callysto.devin.skedulr.types.IView;
import com.callysto.devin.skedulr.types.Section;

public class CalendarView extends View implements IView {
	private Paint paint;
	private MainModel model;
	
	public CalendarView(Context context) {
		super(context);
		constructor();
	}
	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		constructor();
	}
	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		constructor();
	}
	
	private void constructor() {
		paint = new Paint();
		model = MainModel.getInstance();
	}
	
	public CourseSection getCourseFromTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();
		
		int w = getWidth();
		int h = getHeight();
		
		//which day is the user clicking?
		int dayIndex = (int)((x / w) * 5);
		
		for (CourseSection cs : model.getChoices()) {
			try {
				if (! cs.section.getDays()[dayIndex]) continue;
			} catch (Exception ex) {
				ex.printStackTrace();
				break;
			}

			//is there a course scheduled at that time on that day?
			float startY = getStartY(cs.section, h);
			float stopY = getStopY(cs.section, h);
			
			if (startY <= y && y <= stopY) {
				return cs;
			}
		}//for
		
		//no course found at that point
		return null;
	}//getCourseFromTouchEvent
	
	//these two functions calculate where to lay out the courses as a function of
	//the height of the canvas and the range of times. The current range of times
	//is 8:30 AM to 10:00 PM, a spread of 13.5 hours
	private float getStartY(Section s, int h) {
		float start = (float) (s.getStartTimeNum() - 8.5);
		float startY = (float) (start * h / 13.5);
		return startY;
	}//getStartY
	
	private float getStopY(Section s, int h) {
		float end = (float) (s.getEndTimeNum() - 8.5);
		float stopY = (float) (end * h / 13.5);
		return stopY;
	}//getStopY

	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);

		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(18);
		
		int w = getWidth();
		int h = getHeight();

		for (CourseSection cs : model.getChoices()) {
			float startY = getStartY(cs.section, h);
			float stopY = getStopY(cs.section, h);

			String name = cs.course.getSubject() + " " + cs.course.getCatalog();
			String sec = cs.section.getType() + " " + cs.section.getSec();
			
			//iterate over days of the week
			for (int i = 0; i < 5; i += 1) {
				if (cs.section.getDays()[i]) {
					float currentX = i * w / 5;
					drawClass(canvas, paint, currentX, currentX + w / 5, 
							startY, stopY, name, sec);
				}//if
			}//for
		}//for
	}
	
	private void drawClass(Canvas canvas, Paint paint, float x1, float x2,
			float y1, float y2, String name, String sec) {
		drawRoundedRect(canvas, x1, y1, x2, y2, paint);
		
		//write a description of the course
		paint.setColor(Color.BLACK);
		canvas.drawText(name, x1 + 3, y1 + 17, paint);
		canvas.drawText(sec, x1 + 3, y1 + 34, paint);
	}
	
	private void drawRoundedRect(Canvas canvas, float x1, float y1, float x2, float y2, Paint paint) {
		paint.setColor(Color.WHITE);
		canvas.drawRect(x1, y1, x2, y2, paint);
		
		final int SIZE = 10;
		
		//cut out four corners
		//to avoid leftover gray lines, some are adjusted by 1 pixel
		paint.setColor(Color.BLACK);
		canvas.drawRect(x1,        y1 - 1,        x1 + SIZE, y1 + SIZE, paint);
		canvas.drawRect(x2 - SIZE, y1 - 1,        x2,        y1 + SIZE, paint);
		canvas.drawRect(x1,        y2 - SIZE - 1, x1 + SIZE, y2 + 1, paint);
		canvas.drawRect(x2 - SIZE, y2 - SIZE - 1, x2,        y2 + 1, paint);
		
		//fill them in with curves
		//to avoid leftover black lines, just draw the whole circle
		paint.setColor(Color.WHITE);
		canvas.drawArc(new RectF(x1, y1, x1 + SIZE * 2, y1 + SIZE * 2), 180, 360, true, paint);
		canvas.drawArc(new RectF(x2 - SIZE * 2, y1, x2, y1 + SIZE * 2), 270, 360, true, paint);
		canvas.drawArc(new RectF(x1, y2 - SIZE * 2, x1 + SIZE * 2, y2), 90, 360, true, paint);
		canvas.drawArc(new RectF(x2 - SIZE * 2, y2 - SIZE * 2, x2, y2), 0, 360, true, paint);
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
	
	public void updateView() {
		this.invalidate();
	}//updateView
}//CalendarView
