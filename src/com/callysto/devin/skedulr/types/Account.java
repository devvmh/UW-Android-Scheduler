package com.callysto.devin.skedulr.types;

import java.util.Map;

import com.callysto.devin.skedulr.exceptions.AccountCreationError;
import com.callysto.devin.skedulr.exceptions.DuplicateAccountError;
import com.callysto.devin.skedulr.exceptions.HttpPostException;
import com.callysto.devin.skedulr.exceptions.LoginException;
import com.callysto.devin.skedulr.util.ServerInteraction;

/**
 * A user account for the skedulr web service.
 *
 */
public class Account {

	public final String userid;
	public final String password;
	public final String surname;
	public final String givenNames;

	/**
	 * Construct an account based on data already in the database.
	 * 
	 * @param user
	 * @param passwd
	 * @throws HttpPostException 
	 * @throws LoginException 
	 */
	public Account(String user, String passwd) throws HttpPostException, LoginException {
		this.userid = user;
		this.password = passwd;
		
		Map<String, String> result = ServerInteraction.getAccount(user, passwd);
		this.surname = result.get("surname");
		this.givenNames = result.get("given");
	}//constructor #1 - login constructor

	/**
	 * Construct an account that is not yet in the database OR just initialize an account from stored data;
	 * 
	 * @param userid
	 * @param passwd
	 * @param surname
	 * @param givenNames
	 */
	public Account(String user, String passwd, String surname, String givenNames, boolean creatingAccount)
			throws DuplicateAccountError, HttpPostException, AccountCreationError {
		//if you're just loading this from a string description, skip the account creation part
		if (creatingAccount) {
			ServerInteraction.createAccount(user, passwd, surname, givenNames);
		};
		
		this.userid = user;
		this.password = passwd;
		this.surname = surname;
		this.givenNames = givenNames;
	}//constructor #2
}//Account
