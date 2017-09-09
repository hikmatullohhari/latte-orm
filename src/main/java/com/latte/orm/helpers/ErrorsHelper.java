package com.latte.orm.helpers;
/**
 * @author hikmatullohhari
 */
public class ErrorsHelper {
	public static String errorMessages = "";
	public static boolean hasErrors = false;
	public static Exception errors = new Exception();
	
	public static void addSuppressedAndPrintStackTree(Exception e){
		hasErrors = true;
		errors.addSuppressed(e);
		e.printStackTrace();
		
	}
	public static void setErrorMessageAndPrintToConsole(String message){
		hasErrors = true;
		errorMessages = errorMessages + message + "\n";
		System.err.println(message);
	}
	
	public static boolean hasErrors(){
		return hasErrors;
	}
	
	public static Exception getSuppressedErrorException(){
		return errors;
	}
	
	public static String getErrorMessages(){
		return errorMessages;
	}
}
