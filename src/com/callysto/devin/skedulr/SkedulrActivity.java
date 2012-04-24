package com.callysto.devin.skedulr;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callysto.devin.skedulr.model.MainModel;
import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.CourseSection;
import com.callysto.devin.skedulr.types.IView;
import com.callysto.devin.skedulr.util.CalendarView;

public class SkedulrActivity extends Activity implements IView {
	private MainModel model;
	private SharedPreferences savedData;
	
	private ImageButton searchButton;
	private EditText searchBox;
	private CalendarView calendarView;
	private ProgressBar progressBar;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.skedulr);
		findViews();
		addListeners();
		savedData = getSharedPreferences("savedData", MODE_PRIVATE);
		model = MainModel.getInstance();
		
		if (! model.choicesNotLoadedYet) {
			progressBar.setVisibility(View.INVISIBLE);
		}//if

    	model.loadFilters(savedData);
    	model.loadAccount(savedData, this.getApplicationContext());
    	model.loadChoices(savedData);
		
		//get cached copy of course list quickly
		//new GetSerializedCoursesTask().execute(this);
		
		//get the actual course list from the web a bit later
		new CoursesFactoryTask().execute(getBaseContext());
	}//onCreate
	
    public void onStop() {
    	super.onStop();
    	SharedPreferences.Editor prefsEditor = savedData.edit();
    	if (model.isLoggedIn()) {
    		model.saveFilters(prefsEditor);
    		model.saveChoices(prefsEditor);
    		model.saveAccount(prefsEditor);
    		prefsEditor.commit();
    	}
    }//onStop
    
    public void onPause() {
    	super.onPause();
    	model.removeView(this);
    	model.removeView(calendarView);
    }
    
    public void onResume() {
    	super.onResume();
    	searchBox.setText(model.getFilterString());
    	model.addView(this);
    	model.addView(calendarView);
    	
    	//if another activity is more pertinent, go there
    	checkIfLoggedIn();
    	
		if (model.choicesNotLoadedYet) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
			checkIfAnyChoicesYet();
		}//if
    }
    
    private void findViews() {
    	calendarView = (CalendarView) findViewById(R.id.calendar);
    	searchButton = (ImageButton) findViewById(R.id.main_search_button);
    	searchBox = (EditText) findViewById(R.id.main_search_box);
    	progressBar = (ProgressBar) findViewById(R.id.save_selections_pbar);
    }//findViews
    
    private void addListeners() {
		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent (SkedulrActivity.this, CourseCatalogActivity.class);
				startActivity(intent);
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
					//less text than before so recalculate whole list
					model.redoFilteredList();
				} else {
					//more text than before so just remove entries
					model.shortenFilteredList();
				}//if
				//both cases update all views in the model
			}//onTextChanged
		});
		calendarView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent e) {
				CourseSection cs = calendarView.getCourseFromTouchEvent(e);
				if (cs == null) {
					showAddCourseQuestionDialog();
				} else {
					showCourseSectionDialog(cs);
				}
				return false;
			}//onTouch
		});
    }//addListeners
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater ();
    	inflater.inflate(R.menu.skedulr_menu, menu);
    	
    	MenuItem saveChoices = menu.findItem(R.id.save_option_item);
    	saveChoices.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem arg0) {
    			model.saveCoursesAsync(SkedulrActivity.this.getApplicationContext(), progressBar);
    			return false;
    		}
    	});
    	
    	//logout intent 
    	MenuItem logout = menu.findItem(R.id.logout_option_item);
    	logout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				item.setEnabled(false);
		    	boolean success = model.saveCourses(SkedulrActivity.this.getApplicationContext(),
		    			progressBar);
		    	if (success) {
		    		model.logout(savedData.edit());
		    	} else {
		    		showLogoutAnywayDialog();
		    	}//if
		    	item.setEnabled(true);
				return false;
			}
    	});
    	return true;
    }
    
    private void checkIfLoggedIn() {
		if (! model.isLoggedIn()) {
			Intent login = new Intent(this, LoginActivity.class);
			startActivity(login);
		}//if
    }//checkIfLoggedIn
    
    private void checkIfAnyChoicesYet() {
    	if (model.getChoices().size() == 0) {
    		Intent courseCatalog = new Intent(this, CourseCatalogActivity.class);
    		startActivity(courseCatalog);
    	}//if
    }//checkIfAnyChoicesYet

	public void updateView() {
			checkIfLoggedIn();
	}//updateView
	
    private void writeSerializedCourses() {
    	FileOutputStream f = null;
    	ObjectOutputStream out = null;
    	try {
    		f = openFileOutput("courses.object", Context.MODE_PRIVATE);
    		out = new ObjectOutputStream(f);
    		out.writeObject(model.getCourses());
    		out.close();
    	} catch (IOException e) {
		    e.printStackTrace();
    	}//try-catch
    }//writeSerializedCourses
    
    class GetSerializedCoursesTask extends AsyncTask<Context, Void, List<Course>> {
    	Context context;
    	@SuppressWarnings("unchecked")
		protected List<Course> doInBackground(Context... arg0) {
    		context = arg0[0];
        	FileInputStream f = null;
        	ObjectInputStream in = null;
        	
        	//in case it fails, put a workable object
        	LinkedList<Course> courses = new LinkedList<Course>();
        	try {
        		f = openFileInput("courses.object");
        		in = new ObjectInputStream(f);
        		courses = (LinkedList<Course>) in.readObject();
        		in.close();
        	} catch(IOException e) {
        		e.printStackTrace();
        	} catch(ClassNotFoundException e) {
    	    	e.printStackTrace();
        	}
        	return courses;
    	}
    	protected void onPostExecute(List<Course> lst) {
    		//quick check in case the other ASyncTask beat us to the punch
    		if (model.getCourses().size() <= 0) {
        		model.setCourses(lst);
        		model.redoFilteredList();
    		}//if
    		Toast.makeText(context, "Serialized read finished, got " + String.valueOf(lst.size()) + " entries", 
    				Toast.LENGTH_SHORT).show();
    	}//onPostExecute
    }//GetSerializedCoursesTask
	
	class CoursesFactoryTask extends AsyncTask<Context, Integer, List<Course>> {
		Context context;
		protected List<Course> doInBackground(Context... arg0) {
			context = arg0[0];
			List<Course>courses = Course.coursesFactory();
			return courses;
		}//doInBackground
		protected void onPostExecute(List<Course> lst) {
			boolean coursesListChanged = (model.getCourses().size() != 0
					&& model.getCourses().size() != lst.size());
			model.setCourses(lst);
			if (model.getCourses().isEmpty()) {
				//recurse
				new CoursesFactoryTask().execute(context);
			} else {
				//base case
				if (context != null) {
					String synced = context.getResources().getString(R.string.courses_synced);
					Toast.makeText(context, synced, Toast.LENGTH_SHORT).show();
				}
				model.redoFilteredList();
				
				//update the stored list since the Internet version changed
				if (coursesListChanged) {
					writeSerializedCourses();
				}//if
			}//if
		}//onPostExecute
	}//CoursesFactoryTask
	
	private void showCourseSectionDialog(final CourseSection cs) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		String ok = getResources().getString(R.string.ok);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, ok, new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				;
			}
		});
		String delete = getResources().getString(R.string.delete);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, delete, new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				model.removeChoice(cs);
				model.saveChoices(savedData.edit());
			}
		});
		
		String title = cs.course.getSubject() + " " + cs.course.getCatalog();
        dialog.setTitle(title);
        String message = cs.course.getTitle() + "\n";
        message += cs.section.toString();
        dialog.setMessage (message);
        
        dialog.show();
	}
	
	private void showAddCourseQuestionDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		String ok = getResources().getString(R.string.ok);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, ok, new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent (SkedulrActivity.this, CourseCatalogActivity.class);
				startActivity(intent);
			}//onClick
		});
		
		String cancel = getResources().getString(R.string.cancel);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, cancel, new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				;
			}//onClick
		});
		
		String message = getResources().getString(R.string.want_to_add);
		dialog.setMessage(message);
		
		dialog.show();
	}//showAddCourseDialog
	
	private void showLogoutAnywayDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		String ok = getResources().getString(R.string.ok);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, ok, new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				model.logout(savedData.edit());
			}//onClick
		});
		
		String cancel = getResources().getString(R.string.cancel);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, cancel, new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				;
			}//onClick
		});
		
		String message = getResources().getString(R.string.logout_anyway);
		dialog.setMessage(message);
		
		dialog.show();
	}
}//SkedulrActivity
