package com.callysto.devin.skedulr.util;

/*
 * This class is all the static functions that interact with the server: logging in,
 * deleting accounts, getting and replacing course selections on an account, etc.
 */

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callysto.devin.skedulr.R;
import com.callysto.devin.skedulr.exceptions.AccountCreationError;
import com.callysto.devin.skedulr.exceptions.AccountDeletionError;
import com.callysto.devin.skedulr.exceptions.DuplicateAccountError;
import com.callysto.devin.skedulr.exceptions.GetCoursesException;
import com.callysto.devin.skedulr.exceptions.HttpPostException;
import com.callysto.devin.skedulr.exceptions.LoginException;
import com.callysto.devin.skedulr.exceptions.SaveCoursesException;
import com.callysto.devin.skedulr.model.MainModel;
import com.callysto.devin.skedulr.types.Course;
import com.callysto.devin.skedulr.types.CourseSection;

public class ServerInteraction {
	public static void deleteAccount (String user, String passwd) 
			throws HttpPostException, AccountDeletionError {
		String result = HttpUtil.httpPost(
				"http://anlujo.cs.uwaterloo.ca/cs349/deleteAccount.py",
				new String[] { "user_id", "passwd"},
				new String[] { user, passwd });
		
		if (Pattern.matches("<\\?.*\\?><status>OK</status>", result))
			return;
		throw new AccountDeletionError (result);
	}
	
	public static Map<String, String> getAccount (String user, String passwd) 
			throws HttpPostException, LoginException {
		String result = HttpUtil.httpPost(
				"http://anlujo.cs.uwaterloo.ca/cs349/getAccount.py",
				new String[] { "user_id", "passwd"},
				new String[] { user, passwd });
		
		Matcher m = invalidUserOrPass.matcher(result);
		if (m.matches()) {
			throw new LoginException ();
		}
		
		List<String> lst = new LinkedList<String> ();
		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			//get a new instance of parser, convert string to
			//an input source, parse the input, and register 
			//a private class for call backs
			SAXParser sp = spf.newSAXParser();
			InputSource is = new InputSource (new StringReader (result));
			sp.parse(is, new AccountParserCallBacks(lst));
			
			//get the names
			Map<String, String> names = new HashMap<String, String> ();
			names.put("given", lst.get(0));
			names.put("surname", lst.get(1));
			return names;
		} catch(SAXException se) {
			se.printStackTrace();
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}//try-catch
		
		throw new LoginException ();
	}
	
	//for saveCourses()
	private static String [] makeParameterArray(Vector<CourseSection> choices) {
		int numberOfParameters = 2 + 3 * choices.size();
		String [] params = new String [numberOfParameters];
		params[0] = "user_id";
		params[1] = "passwd";
		for (int i = 2; i < numberOfParameters; i += 3) {
			params[i] = "subject";
			params[i+1] = "catalog";
			params[i+2] = "section";
		}//for
		return params;
	}//makeParameterArray
	
	//for saveCourses()
	private static String [] makeValueArray(String user, String passwd, Vector<CourseSection> choices) {
		int numberOfParameters = 2 + 3 * choices.size();
		String [] values = new String [numberOfParameters];
		values[0] = user;
		values[1] = passwd;
		int i = 2;
		for (CourseSection cs : choices) {
			values[i] = cs.course.getSubject();
			values[i + 1] = cs.course.getCatalog();
			values[i + 2] = cs.section.getSec();
			i += 3;
		}//for
		return values;
	}//makeValueArray

	//run on another thread, report on calling context when done
	//it passes exceptions back to this thread to be thrown
	//progressbar is optional
	public static void saveCoursesAsync(final String user, final String passwd, 
			final Vector<CourseSection> choices, Context context, final ProgressBar pb) {
		if (pb != null) {
			pb.setVisibility(View.VISIBLE);
		}
		
		(new AsyncTask<Context, Void, Exception>() {
			Context context;
			protected Exception doInBackground(Context... arg0) {
				this.context = arg0[0];
				String [] params = makeParameterArray (choices);
				String [] values = makeValueArray (user, passwd, choices);
				
				try {
				String result = HttpUtil.httpPost(
						"http://anlujo.cs.uwaterloo.ca/cs349/replaceCourses.py",
						params, values);
				if (! Pattern.matches("<\\?.*\\?><status>OK</status>", result)) {
					return new SaveCoursesException (result);
				}//if
				} catch (Exception e) {
					return e;
				}

				return null;
			}
			
			protected void onPostExecute(Exception e) {
				Resources r = context.getResources();
				if (e == null) {
					Toast.makeText(context, r.getString(R.string.courses_saved), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, r.getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
				}

				if (pb != null) {
					pb.setVisibility(View.INVISIBLE);	
				}
			}
		
		}).execute(context);
	}//saveCoursesAsync
	
	//progressbar is optional
	public static boolean saveCourses(final String user, final String passwd, 
			Vector<CourseSection> choices, Context context, ProgressBar pb) {
		if (pb != null) {
			pb.setVisibility(View.VISIBLE);
		}
		String [] params = makeParameterArray (choices);
		String [] values = makeValueArray (user, passwd, choices);
		boolean returnValue = true;
		Resources r = context.getResources();
		
		try {
			String result = HttpUtil.httpPost(
					"http://anlujo.cs.uwaterloo.ca/cs349/replaceCourses.py",
					params, values);
			if (! Pattern.matches("<\\?.*\\?><status>OK</status>", result)) {
				Toast.makeText(context, r.getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
				returnValue = false;
			}//if
		} catch (Exception e) {

			Toast.makeText(context, r.getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
				returnValue = false;
		}
		
		if (pb != null) {
			pb.setVisibility(View.INVISIBLE);	
		}
		
		if (returnValue == true) {
			Toast.makeText(context,  r.getString(R.string.courses_saved), Toast.LENGTH_SHORT).show();
		}//if
		
		return returnValue;
	}//saveCourses
	
	//get them as an AsyncTask to save time. Publish exceptions as progress updates.
	public static void getCourseSelections (final String user, final String pass, final Context context) {
		(new AsyncTask<Context, Exception, Vector<CourseSection>>() {
			Context context;
			Resources r;
			protected Vector<CourseSection> doInBackground(Context...contexts) {
				this.context = contexts[0];
				r = context.getResources();
				String result;
				try {
					result = HttpUtil.httpPost(
							"http://anlujo.cs.uwaterloo.ca/cs349/getCourses.py",
							new String[] { "user_id", "passwd"},
							new String[] { user, pass });
				} catch (HttpPostException e) {
					publishProgress(e);
					return null;
				}
				Matcher m = invalidUserOrPass.matcher(result);
				if (m.matches()) {
					publishProgress(new LoginException());
				}//if

				//get a factory
				SAXParserFactory spf = SAXParserFactory.newInstance();
				try {
					List<Course> courses = Course.coursesFactory();

					Vector<CourseSection> choices = new Vector<CourseSection>();
					//get a new instance of parser, convert string to
					//an input source, parse the input, and register 
					//a private class for call backs
					SAXParser sp = spf.newSAXParser();
					InputSource is = new InputSource (new StringReader (result));
					sp.parse(is, new PastSelectionParserCallBacks(choices, courses));

					return choices;
				} catch(SAXException se) {
					se.printStackTrace();
				} catch(ParserConfigurationException pce) {
					pce.printStackTrace();
				} catch (IOException ie) {
					ie.printStackTrace();
				}//try-catch
				publishProgress (new GetCoursesException (result));
				return null;
			}//doInBackground
			
			protected void onProgressUpdate(Exception... e) {
				Toast.makeText(context, r.getString(R.string.error_retrieving_choices), Toast.LENGTH_SHORT).show();
			}//onProgressUpdate

			protected void onPostExecute(Vector<CourseSection> choices) {
				if (choices != null) {
					for (CourseSection cs : choices) {
						MainModel.getInstance().addChoice(cs);
					}//for
					Toast.makeText(context, r.getString(R.string.choices_retrieved), Toast.LENGTH_SHORT).show();
				}//if
			}//onPostExecute
		}).execute(context);
	}//getCourseSelections
	
	public static boolean createAccount (String user, String passwd, String surname, 
			String givenNames) throws HttpPostException, DuplicateAccountError, AccountCreationError {
		String result = HttpUtil.httpPost(
				"http://anlujo.cs.uwaterloo.ca/cs349/createAccount.py",
				new String[] { "user_id", "passwd", "surname", "given_names" },
				new String[] { user, passwd, surname, givenNames });

		if (Pattern.matches("<\\?.*\\?><status>OK</status>", result))
			return true;

		Matcher m = accountExistsRE.matcher(result);
		if (m.matches())
			throw new DuplicateAccountError(m.group(1));
		else
			throw new AccountCreationError(result);
	}
	
	private static Pattern invalidUserOrPass = Pattern
			.compile("<\\?.*\\?><error>Invalid userid or password.</error>");
		
	private static Pattern accountExistsRE = Pattern
		.compile("<\\?.*\\?><error>Account for user_id ([a-zA-Z0-9]+) already exists.</error>");
}//ServerInteraction class
