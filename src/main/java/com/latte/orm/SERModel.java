package com.latte.orm;
/**
 * @author hikmatullohhari
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import com.latte.orm.helpers.ErrorsHelper;
import com.latte.orm.helpers.Helper;

public class SERModel<T> extends GenericModel<T>{
	public SERModel(Class<?> entityClass, String fileLocation) {
		super(entityClass, fileLocation);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Model<T> startMapping(){
		File serFile = new File(Helper.filePath(fileLocation)+Helper.fileName(fileLocation)+((Helper.fileExtension(fileLocation) == "") ? "" : ".ser"));
		try {
			FileInputStream fin = new FileInputStream(serFile);
			ObjectInputStream in = new ObjectInputStream(fin);
			records = (List<T>) in.readObject();
			in.close();
			fin.close();
		} catch (ClassNotFoundException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		} catch (IOException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		}
		return this;
	}
	
	@Override
	public void save(){
		if(!ErrorsHelper.hasErrors()){
			exportToSER(fileLocation);
		} else {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] errors found, save failed.");
		}
	}
}
