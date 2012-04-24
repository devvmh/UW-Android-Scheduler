package com.callysto.devin.skedulr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callysto.devin.skedulr.exceptions.HttpPostException;
import com.callysto.devin.skedulr.exceptions.LoginException;
import com.callysto.devin.skedulr.model.MainModel;
import com.callysto.devin.skedulr.types.Account;
import com.callysto.devin.skedulr.types.IView;

public class LoginActivity extends Activity implements IView {
	private Button loginButton;
	private Button createAccountButton;
	private EditText usernameTF;
	private EditText passwordTF;
	private ProgressBar progressBar;
	private SharedPreferences savedData;
	private MainModel model;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        savedData = getSharedPreferences("savedData", MODE_PRIVATE);
        findViews();
        addListeners();
        model = MainModel.getInstance();
        model.addView(this);
    }//onCreate   
    
    public void onPause() {
    	super.onPause();
    	String username = (String) usernameTF.getText().toString();
    	SharedPreferences.Editor prefsEditor = savedData.edit();
    	prefsEditor.putString("username", username);
    	prefsEditor.commit();
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	//this goes in onDestroy because LoginActivity calls CreateAccountActivity and gets paused,
    	//and it still needs to get updated if the user creates an account, thereby logging in
    	model.removeView(this);
    }
    
    public void onResume() {
    	super.onResume();
    	String username = savedData.getString("username", "");
    	usernameTF.setText(username);
    }
    
  	//this way back button events will "exit" skedulr rather than trapping the user in the login screen
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void findViews() {
    	loginButton = (Button) findViewById(R.id.login_button);
    	createAccountButton = (Button) findViewById(R.id.login_create_account_button);
    	usernameTF = (EditText) findViewById(R.id.login_username);
    	passwordTF = (EditText) findViewById(R.id.login_password);
    	progressBar = (ProgressBar) findViewById(R.id.login_progressbar);
    }//findViews
    
    private void addListeners() {
    	loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				login();
			}
    	});
    	createAccountButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent createAccountIntent = new Intent(LoginActivity.this, 
						CreateAccountActivity.class);
				startActivity(createAccountIntent);
			}
    	});
    }//addListeners
    
    void login() {
    	String user = usernameTF.getText().toString();
    	String pass = passwordTF.getText().toString();
    	hideKeyboard(); //so the toasts will show up better
    	
    	if (pass.length() < 4) {
    		toast(getResources().getString(R.string.passwords_4));
    		return;
    	}
    	if (user.length() < 4) {
    		toast(getResources().getString(R.string.usernames_4));
    		return;
    	}
    	
    	//asynctask inner class
    	progressBar.setVisibility(View.VISIBLE);
    	new LoginTask().execute(user, pass);
    }
    
    private void toast(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    private void hideKeyboard() {
    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	 mgr.hideSoftInputFromWindow(usernameTF.getWindowToken(), 0);
    }
    
    public void updateView() {
    	if (model.isLoggedIn()) {
    		finish();
    	}//if
    }//updateView
    
    class LoginTask extends AsyncTask<String, String, Account> {
    	String errorMessage;
    	Resources r = LoginActivity.this.getResources();
		protected Account doInBackground(String... args) {
	    	Account acct;
	    	String user = args[0];
	    	String pass = args[1];
			try {
				acct = new Account(user, pass);
			} catch (HttpPostException e) {
				publishProgress(r.getString(R.string.cant_connect));
				return null;
			} catch (LoginException e) {
				
				publishProgress(r.getString(R.string.cant_login_password));
				return null;
			}//try-catch
			
	    	return acct;
		}//doInBackground
		protected void onProgressUpdate(String... args) {
			errorMessage = args[0];
		}//onProgressUpdate
    	protected void onPostExecute(Account acct) {
        	if (acct != null) {
        		//true means selections exist on server
        		boolean success = model.login(acct, true, LoginActivity.this.getApplicationContext());
        		SharedPreferences.Editor ed = savedData.edit();
        		model.saveAccount(ed);
        		ed.commit();
        		if (! success) {
        			toast(r.getString(R.string.error_retrieving_choices));
        		}
        	} else {
        		toast(errorMessage);
        	}//if
        	progressBar.setVisibility(View.INVISIBLE);
    	}//onPostExecute
    }//LoginTask
}//LoginActivity