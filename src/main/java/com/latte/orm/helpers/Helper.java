package com.latte.orm.helpers;
/**
 * @author hikmatullohhari
 */
public class Helper {
	public static String filePath(String fileLocation){
		String[] pwd = fileLocation.split("/");
		String path = "";
		for(int i=0; i<pwd.length-1;i++){
			path = path + pwd[i] + "/";
		}
		return path;
	}
	
	public static String fileName(String fileLocation){
		String[] pwd = fileLocation.split("/");
		String[] fileNameFull = pwd[pwd.length-1].split("\\.");
		String fileName = "";
		if(fileNameFull.length >1){
			for(int i=0; i<fileNameFull.length-1;i++){
				fileName = fileName + fileNameFull[i];
				if(i<fileNameFull.length-2){
					fileName = fileName + ".";
				}
			}
		} else {
			fileName = fileNameFull[0];
		}
		return fileName; 
	}
	
	public static String fileExtension(String fileLocation){
		String[] pwd = fileLocation.split("/");
		String[] fileNameFull = pwd[pwd.length-1].split("\\.");
		if(fileNameFull.length > 1){
			return fileNameFull[fileNameFull.length-1];
		} else {
			return "";
		}
	}
}
