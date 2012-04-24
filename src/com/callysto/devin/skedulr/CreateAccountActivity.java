package com.callysto.devin.skedulr;

import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callysto.devin.skedulr.exceptions.AccountCreationError;
import com.callysto.devin.skedulr.exceptions.DuplicateAccountError;
import com.callysto.devin.skedulr.exceptions.HttpPostException;
import com.callysto.devin.skedulr.model.MainModel;
import com.callysto.devin.skedulr.types.Account;
import com.callysto.devin.skedulr.types.IView;

public class CreateAccountActivity extends Activity implements IView {
	private Button createAccountButton;
	private Button cancelButton;
	private EditText nameTF;
	private EditText usernameTF;
	private EditText passwordTF1;
	private EditText passwordTF2;
	private ProgressBar progressBar;
	private SharedPreferences savedData;
	private MainModel model;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
        savedData = getSharedPreferences("savedData", MODE_PRIVATE);
        findViews();
        addListeners();
        model = MainModel.getInstance();
    }//onCreate
    
    public void onPause() {
    	super.onPause();
    	String user = usernameTF.getText().toString();
    	String name = nameTF.getText().toString();
    	SharedPreferences.Editor prefsEditor = savedData.edit();
    	prefsEditor.putString("username", user);
    	prefsEditor.putString("name", name);
    	prefsEditor.commit();
    	model.removeView(this);
    }
    
    public void onResume() {
    	super.onResume();
    	String usernameString = savedData.getString("username", "");
    	String nameString = savedData.getString("name", "");
    	usernameTF.setText(usernameString);
    	nameTF.setText(nameString);
    	model.addView(this);
    }
    
    private void findViews() {
    	createAccountButton = (Button) findViewById(R.id.create_account_button);
    	cancelButton = (Button) findViewById(R.id.create_account_cancel);
    	nameTF = (EditText) findViewById(R.id.create_account_name);
    	usernameTF = (EditText) findViewById(R.id.create_account_username);
    	passwordTF1 = (EditText) findViewById(R.id.create_account_password);
    	passwordTF2 = (EditText) findViewById(R.id.create_account_repeat_password);
    	progressBar = (ProgressBar) findViewById(R.id.create_account_progressbar);
    }
    
    private void addListeners() {
    	cancelButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			finish();
    		}
    	});
    	createAccountButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			createAccount();	
    		}
    	});
    }
    
    void createAccount() {
    	String user = usernameTF.getText().toString();
    	String name = nameTF.getText().toString();
    	String pass = passwordTF1.getText().toString();
    	hideKeyboard(); //so toasts will show up better
    	
    	Resources r = getResources();
    	
    	//validate
    	if (! pass.equals(passwordTF2.getText().toString())) {
    		toast(r.getString(R.string.passwords_match));
    		return;
    	}
    	if (pass.length() < 4) {
    		toast(r.getString(R.string.passwords_4));
    		return;
    	}
    	if (user.length() < 4) {
    		toast(r.getString(R.string.usernames_4));
    		return;
    	}
    	if (name.length() < 1) {
    		toast(r.getString(R.string.no_name));
    		return;
    	}
    	
    	//split the names
    	StringTokenizer st = new StringTokenizer(name);
    	int numNames = st.countTokens();
    	String givenNames = "";
    	for (int i = 1; i <= numNames - 1; i += 1) {
    		givenNames += st.nextToken();
    		if (i != numNames - 1) givenNames += " ";
    	}
    	String surname = st.nextToken();
    	
    	Account acct;
    	try {
    		//the "true" means we're going to try to create the account on the server
    		progressBar.setVisibility(View.VISIBLE);
    		acct = new Account(user, pass, surname, givenNames, true);
    	} catch (AccountCreationError e) {
    		toast(r.getString(R.string.account_error));
    		progressBar.setVisibility(View.INVISIBLE);
    		return;
    	} catch (DuplicateAccountError e) {
    		toast (r.getString(R.string.account_exists));
    		progressBar.setVisibility(View.INVISIBLE);
    		return;
    	} catch (HttpPostException e) {
    		toast(r.getString(R.string.cant_connect));
    		progressBar.setVisibility(View.INVISIBLE);
    		return;
    	}//try-catch
    	
    	//this line will end this activity
    	model.login(acct, false, this.getApplicationContext()); //false because we aren't loading selections from server
    	SharedPreferences.Editor ed = savedData.edit();
		model.saveAccount(ed);
		ed.commit();
    }
    
    private void toast(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }//toast
    
    private void hideKeyboard() {
    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
   	 mgr.hideSoftInputFromWindow(usernameTF.getWindowToken(), 0);
   }//hideKeyboard
    
    public void updateView() {
    	if (model.isLoggedIn()) {
    		finish();
    	}//if
    }//updateView
}//CreateAccountActivity
