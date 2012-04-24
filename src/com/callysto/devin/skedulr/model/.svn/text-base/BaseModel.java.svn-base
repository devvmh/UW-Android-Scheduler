package com.callysto.devin.skedulr.model;

import java.util.Vector;

import com.callysto.devin.skedulr.types.IView;

/*
 * Manage the interaction between models and IViews with this functionality
 */

public class BaseModel {
	private Vector<IView> views;
	
	protected BaseModel() {
		views = new Vector<IView>();
	}
	
	public void addView(IView v) {
		views.add(v);
		v.updateView();
	}// addView

	public void removeView(IView v) {
		views.remove(v);
	}// removeView

	protected void updateAllViews() {
		for (IView v : views) {
			v.updateView();
		}// for
	}// updateAllViews
}
