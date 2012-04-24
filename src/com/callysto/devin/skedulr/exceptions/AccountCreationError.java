package com.callysto.devin.skedulr.exceptions;

/**
 * An exception for signaling a general problem with creating an account.
 */
@SuppressWarnings("serial")
public class AccountCreationError extends Error {
	public AccountCreationError(String msg) {
		super(msg);
	}
}