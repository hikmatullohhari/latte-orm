package com.latte.orm;
/**
 * @author hikmatullohhari
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import com.latte.orm.annotations.OnMapping;
import com.latte.orm.helpers.ErrorsHelper;
import com.latte.orm.helpers.Helper;
import com.latte.orm.helpers.ReflectionHelper;

public class CSVModel<T> extends GenericModel<T>{
	private String delimiter = ","+REGEX_ESCAPE_QUOTE;
	
	public CSVModel(Class<?> entityClass, String fileLocation) {
		super(entityClass, fileLocation);
	}
	
	public CSVModel<T> delimiter(String delimiter){
		this.delimiter = delimiter +REGEX_ESCAPE_QUOTE;
		return this;
	}
	
	@Override
	public Model<T> startMapping(){
		boolean isMappingSuccess = true;
		try {
			String readline;
			int rowNumber = 1;
			BufferedReader bf;
			records = new ArrayList<T>();
			bf = new BufferedReader(new FileReader(new File(fileLocation)));
			readline = bf.readLine();
			columnNamesInCSV = (readline == null) ? null : readline.split(delimiter, -1);
			columnNames = (readline == null) ? null : readline.toLowerCase().split(delimiter, -1);
			for(int i=0;i<columnNames.length;i++){
				columnNames[i] = columnNames[i].replace(" ", "").replaceAll("[^A-Za-z0-9]", "_");
			}
			Method[] method = entityClass.getDeclaredMethods();
			
			whileloop:
			while((readline = bf.readLine()) != null){
				Object[] columns = readline.toString().split(delimiter,-1);
				@SuppressWarnings("unchecked")
				T newEntityClassInstance = (T) entityClass.newInstance();
				
				for(Method m: method){
					if(m.getName().contains("set")){
						String methodName = m.getName().replace("set", "").toLowerCase();
						
						if(Arrays.asList(columnNames).contains(methodName)){
							int index = Arrays.asList(columnNames).indexOf(methodName);
							String rawValue = columns[index].toString().replace("\"", "");
							// Add to instance then invoke
							m.invoke(newEntityClassInstance, ReflectionHelper.autocast(m, rawValue));
						} else {
							ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Method -> "+methodName+" not found in ColumnName of CSV file -> "+Arrays.asList(columnNames));
							break whileloop;
						}
					}
				}
				
				OnMapping onMapping = null;
				if(entityClass.getAnnotation(OnMapping.class) == null){ //if not declared, set check() true
					onMapping = new OnMapping() {
						public Class<? extends Annotation> annotationType() {
							return OnMapping.class;
						}
						public boolean check() {
							return true;
						}
					};
				} else {
					onMapping = entityClass.getAnnotation(OnMapping.class);
				}
				
				if(onMapping.check()){
					if(CheckAnnotationConstraints(newEntityClassInstance)){
						records.add(newEntityClassInstance);
					} else {
						ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] ON ROW -> "+ rowNumber);
						isMappingSuccess = false;
					}
				} else {
					records.add(newEntityClassInstance);
				}
				
				rowNumber++;
			}
			bf.close();
		} catch (FileNotFoundException e) {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] File "+fileLocation+" not found.");
		} catch (InstantiationException e) {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Can't instantiate class "+entityClass.getSimpleName()+".class because empty constructor is not defined in that class.");
		} catch (IllegalAccessException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		} catch (IllegalArgumentException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		} catch (InvocationTargetException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		} catch (IOException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		}
		if(!isMappingSuccess){
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Mapping Failed.");
			records = new ArrayList<T>();
		}
		return this;
	}
	
	@Override
	public void save(){
		if(!ErrorsHelper.hasErrors()){
			exportToCSV(fileLocation);
		} else {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] errors found, save failed. ");
		}
	}
}
