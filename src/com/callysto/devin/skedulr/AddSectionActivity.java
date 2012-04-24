package com.callysto.devin.skedulr;

import java.util.Vector;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.callysto.devin.skedulr.model.AddSectionModel;
import com.callysto.devin.skedulr.model.MainModel;
import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.CourseSection;
import com.callysto.devin.skedulr.types.IView;
import com.callysto.devin.skedulr.types.Section;

public class AddSectionActivity extends Activity implements IView {
	private MainModel model;
	private AddSectionModel sectionsModel;
	
	private static final int START_TIME_DIALOG = 0;
	private static final int END_TIME_DIALOG = 1;
	
	private TextView courseTitle;
	private ListView sectionsList;
	private Button startTimeButton;
	private Button endTimeButton;
	private CheckBox labCheckbox;
	private CheckBox lecCheckbox;
	private CheckBox tutCheckbox;
	
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_section);
		
		model = MainModel.getInstance();
		sectionsModel = AddSectionModel.getInstance();
		sectionsModel.redoFilteredList();
		
		String title = getResources().getString(R.string.add_section);
		title += ": " + AddSectionModel.getCurrentCourse().getSubject();
		title += " " + AddSectionModel.getCurrentCourse().getCatalog();
		this.setTitle(title);
		
		findViews();
		courseTitle.setText(AddSectionModel.getCurrentCourse().getTitle());
		addListeners();
		fillData();
	}//onCreate
	
	public void onPause() {
		super.onPause();
		model.removeView(this);
		sectionsModel.removeView(this);
	}
	
	public void onResume() {
		super.onResume();
		model.addView(this);
		sectionsModel.addView(this);
	}
	
	private void findViews() {
		courseTitle = (TextView) findViewById(R.id.course_title);
		sectionsList = (ListView) findViewById(R.id.sections_listview);
		startTimeButton = (Button) findViewById(R.id.add_section_starttime_chooser);
		endTimeButton = (Button) findViewById(R.id.add_section_endtime_chooser);
		labCheckbox = (CheckBox) findViewById(R.id.lab_checkbox);
		lecCheckbox = (CheckBox) findViewById(R.id.lec_checkbox);
		tutCheckbox = (CheckBox) findViewById(R.id.tut_checkbox);
	}
	
	private void addListeners() {
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
    	labCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton view, boolean isChecked) {
				if (isChecked != sectionsModel.isLabEnabled()) {
					sectionsModel.setIsLabEnabled(isChecked);
				}
			}
    	});
    	lecCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton view, boolean isChecked) {
				if (isChecked != sectionsModel.isLecEnabled()) {
					sectionsModel.setIsLecEnabled(isChecked);
				}
			}
    	});
    	tutCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton view, boolean isChecked) {
				if (isChecked != sectionsModel.isTutEnabled()) {
					sectionsModel.setIsTutEnabled(isChecked);
				}
			}
    	});
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
    				sectionsModel.redoFilteredList();
				}
    		});
    	}//if
    	return dialog;
    }

    private void fillData() {
        	//get the data
        	Section [] sectionsArray;
    		Vector<Section> sectionsVector = sectionsModel.getFilteredSections();
        	if (sectionsVector.isEmpty()) {
        		sectionsArray = new Section[1];
        		String nosecs = getResources().getString(R.string.no_section_matches);
        		sectionsArray[0] = new Section(nosecs, "", "", "0000000", "", "");
        		sectionsList.setEnabled(false);
        	} else {
        		sectionsArray = new Section [sectionsVector.size()];
        		for (int i = 0; i < sectionsVector.size(); i += 1) {
        			sectionsArray[i] = sectionsVector.get(i);
        		}
        		sectionsList.setEnabled(true);
        	}//if
        	
        	final Section [] sectionsArrayData = sectionsArray;
        	//put it in the listview
        	ArrayAdapter<Section> sectionsAdapter = 
        			new ArrayAdapter<Section>(this, R.layout.add_section_row, sectionsArray) {
            	public View getView(int position, View convertView, ViewGroup parent) {
            		View row;
             
            		if (null == convertView) {		
            			LayoutInflater inflater = (LayoutInflater)getSystemService
            				      (Context.LAYOUT_INFLATER_SERVICE);
    					row =  inflater.inflate(R.layout.add_section_row, null);
            		} else {
            			row = convertView;
            		}
            		
    				Section s = sectionsArrayData[position];
    				Course c = AddSectionModel.getCurrentCourse();
    				final CourseSection cs = new CourseSection(c, s);
             
            		TextView tv = (TextView) row.findViewById(R.id.text1);
            		tv.setText(s.toString());
            		tv.setOnClickListener(new OnClickListener() {
            			public void onClick(View v) {
            				boolean addSuccessful = model.addChoice(cs);
            				if (addSuccessful) {
            					toast("Course added!");
            					SharedPreferences savedData = getSharedPreferences("savedData", MODE_PRIVATE);
            					model.saveChoices(savedData.edit());
            					setResult(RESULT_OK);
            					if (model.getChoices().size() == 6) {
            						String warning = getResources().getString(R.string.warning_at_6);
            						toast(warning);
            					}
            					finish();
            				} else if (model.getChoices().size() == 6) {
            					String sorry = getResources().getString(R.string.sorry_at_6);
            					toast(sorry);
            				}
            			}
            	    });
            		
            		
    				if (model.doesItConflict(cs)) {
    					boolean done = false;
    					for (CourseSection choice : model.getChoices()) {
    						if (c.getCatalog().equals(choice.course.getCatalog()) &&
    								c.getSubject().equals(choice.course.getSubject()) &&
    								s.getSec().equals(choice.section.getSec())) {
    							tv.setTextColor(Color.GREEN);
    							done = true;
    							break;
    						}
    					}
    					if (! done) {
    						tv.setTextColor(Color.RED);
    					}//if
    				}
             
            		return row;
            	}
            };
        	sectionsList.setAdapter(sectionsAdapter);
    }

    private void toast(String text) {
    	Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
    }//toast
    
	public void updateView() {
		//go to login screen if you logged out
		if (! model.isLoggedIn()) finish();
		
		fillData();
		labCheckbox.setChecked(sectionsModel.isLabEnabled());
		lecCheckbox.setChecked(sectionsModel.isLecEnabled());
		tutCheckbox.setChecked(sectionsModel.isTutEnabled());
		startTimeButton.setText(model.getStartTimeString());
		endTimeButton.setText(model.getEndTimeString());
	}

}
