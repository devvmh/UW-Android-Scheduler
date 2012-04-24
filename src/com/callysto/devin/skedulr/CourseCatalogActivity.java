package com.callysto.devin.skedulr;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.callysto.devin.skedulr.model.AddSectionModel;
import com.callysto.devin.skedulr.model.MainModel;
import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.CourseSection;
import com.callysto.devin.skedulr.types.IView;
import com.callysto.devin.skedulr.types.Section;

public class CourseCatalogActivity extends Activity implements IView {
	private MainModel model;
	
	private static final int START_TIME_DIALOG = 0;
	private static final int END_TIME_DIALOG = 1;
	
	private static final int ADD_SECTION_REQUEST = 0;
	
	private ListView coursesList;
	private ImageButton addCourseButton;
	private EditText searchBox;
	private Button startTimeButton;
	private Button endTimeButton;
	private ProgressBar progressBar;
	
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.course_catalog);
		model = MainModel.getInstance();
		findViews();
		addListeners();
		fillData();
	}//onCreate
	
	public void onPause() {
		super.onPause();
		model.removeView(this);
	}
	
	public void onResume() {
		super.onResume();
		model.addView(this);
		searchBox.setText(model.getFilterString());
	}
	
  	//this way back button events will "exit" skedulr  if you
	//have no courses chosen
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && model.getChoices().size() == 0) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void addSection(Course c) {
		AddSectionModel.setCurrentCourse(c);
		Intent i = new Intent(CourseCatalogActivity.this.getBaseContext(), 
				AddSectionActivity.class);
		startActivityForResult(i, 0);
    }
	
	public void findViews() {
		coursesList = (ListView) findViewById(R.id.search_results);
		addCourseButton = (ImageButton) findViewById(R.id.add_courses_add_button);
		((ImageButton) findViewById(R.id.add_courses_search_button)).setEnabled(false);
		searchBox = (EditText) findViewById(R.id.add_courses_search_box);
		startTimeButton = (Button) findViewById(R.id.start_time_button);
		endTimeButton = (Button) findViewById(R.id.end_time_button);
		progressBar = (ProgressBar) findViewById(R.id.add_courses_progressbar);
	}
	
	public void addListeners() {
		addCourseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Course c = (Course) model.getCourseFromSearch(getBaseContext());
				if (c == null) return;
				
				addSection(c);
			}//onClick
		});
		searchBox.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start,
					int before, int count) {}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				model.setFilterString(s.toString());
				if (count < before) {
					//less text than before
					model.redoFilteredList();
				} else {
					//more text than before
					model.shortenFilteredList();
				}//if
				//both cases update all views in the model
			}//onTextChanged
		});
    	coursesList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Course c = (Course) parent.getAdapter().getItem(position);
				if (c.toString().startsWith("No c")) return; // fake course
				addSection(c);
			}
    	});
    	startTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(START_TIME_DIALOG);
			}
    	});
    	endTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(END_TIME_DIALOG);
			}
    	});
	}//addListeners
	
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater ();
    	inflater.inflate(R.menu.skedulr_menu, menu);
    	
    	//logout intent 
    	MenuItem logout = menu.findItem(R.id.logout_option_item);
    	logout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem arg0) {
				boolean success = model.saveCourses(CourseCatalogActivity.this.getApplicationContext(), progressBar);
				if (success) {
					SharedPreferences savedData = getSharedPreferences("savedData", MODE_PRIVATE);
					model.logout(savedData.edit());
					finish();
				}//if
				return false;
			}
    	});
    	MenuItem saveChoices = menu.findItem(R.id.save_option_item);
    	saveChoices.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem arg0) {
    			model.saveCoursesAsync(CourseCatalogActivity.this.getApplicationContext(), progressBar);
    			return false;
    		}
    	});
    	return true;
    }
	
    private void fillData() {
    	//get the data
    	Course [] coursesArray;
    	if (model.getCourses().isEmpty()) {
    		//hack to get listview to show loading message
    		coursesArray = new Course [1];
    		String noCourses = getResources().getString(R.string.no_courses_yet);
    		coursesArray[0] = new Course(noCourses, "", "", null);
    		coursesList.setEnabled(false);
    	} else if (model.getFilteredCourses().isEmpty()) {
    		coursesArray = new Course[1];
    		String noMatches = getResources().getString(R.string.no_course_matches);
    		coursesArray[0] = new Course(noMatches, "", "", null);
    		coursesList.setEnabled(false);
    	} else {
    		coursesArray = new Course [model.getFilteredCourses().size()];
    		for (int i = 0; i < model.getFilteredCourses().size(); i += 1) {
    			coursesArray[i] = model.getFilteredCourses().get(i);
    		}
    		coursesList.setEnabled(true);
    	}//if
    	
    	//put it in the listview
    	ArrayAdapter<Course> coursesAdapter = new ArrayAdapter<Course>(this, R.layout.course_catalog_row, coursesArray) {
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View row;
         
        		if (null == convertView) {		
        			LayoutInflater inflater = (LayoutInflater)getSystemService
        				      (Context.LAYOUT_INFLATER_SERVICE);
					row =  inflater.inflate(R.layout.course_catalog_row, null);
        		} else {
        			row = convertView;
        		}

        		Course c = getItem(position);
        		TextView tv = (TextView) row.findViewById(R.id.text1);
        		tv.setText(c.toString());
        		tv.setTextColor(Color.WHITE);

        		String catalog = c.getCatalog();
        		String subject = c.getSubject();

        		//mark chosen courses with green
        		boolean done = false;
        		for (CourseSection cs : model.getChoices()) {
        			if (cs.course.getSubject().equals(subject) &&
        					cs.course.getCatalog().equals(catalog)) {
        				tv.setTextColor(Color.GREEN);
        				done = true;
        				break;
        			}
        		}//for
        		
        		//if you didn't set it to green, maybe you need to set it to red
        		//because all of its sections conflict
        		if (! done && c.getSections() != null) {
        			boolean allConflicted = true;
        			for (Section s : c.getSections()) {
        				CourseSection cs = new CourseSection(c, s);
        				if (! model.doesItConflict(cs)) {
        					allConflicted = false;
        					break;
        				}
        			}            		
            		if (allConflicted) {
            			tv.setTextColor(Color.RED);
            		}//if
        		}//if

        		return row;
        	}
        };
    	coursesList.setAdapter(coursesAdapter);
    }
    
    private TimePickerDialog.OnTimeSetListener startTimeSetListener =
    		new TimePickerDialog.OnTimeSetListener() {
    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    		model.setStartTime(hourOfDay, minute);
    	}//onTimeSet
    };
    private TimePickerDialog.OnTimeSetListener endTimeSetListener =
    		new TimePickerDialog.OnTimeSetListener() {
    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    		model.setEndTime(hourOfDay, minute);
    	}//onTimeSet
    };

    //two time picker dialogs that update the model when they are dismissed
    protected Dialog onCreateDialog(int id) {
    	TimePickerDialog dialog = null;
    	switch (id) {
    	case START_TIME_DIALOG:
    		dialog = new TimePickerDialog(this,
    				startTimeSetListener, model.getStartTimeHour(), model.getStartTimeMinute(), false);
    		break;
    	case END_TIME_DIALOG:
    		dialog = new TimePickerDialog(this,
    				endTimeSetListener, model.getEndTimeHour(), model.getEndTimeMinute(), false);
    		break;
    	}//switch
    	
    	if (dialog != null) {
    		dialog.setOnDismissListener(new OnDismissListener() {
    			public void onDismiss(DialogInterface dialog) {
    				model.redoFilteredList();
				}
    		});
    	}//if
    	return dialog;
    }
    

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ADD_SECTION_REQUEST:
    		if (resultCode == RESULT_OK) {
    			//go back to the calendar activity
    			finish();
    		}
    		break;
    	default:
    		break;
    	}
    }

	public void updateView() {
		if (! model.isLoggedIn()) finish();
		fillData();
		startTimeButton.setText(model.getStartTimeString());
		endTimeButton.setText(model.getEndTimeString());
	}
}
