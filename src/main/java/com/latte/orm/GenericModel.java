package com.latte.orm;
/**
 * @author hikmatullohhari
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.latte.orm.annotations.*;
import com.latte.orm.helpers.ErrorsHelper;
import com.latte.orm.helpers.Helper;

public class GenericModel<T> implements Model<T> {
	protected static final String REGEX_ESCAPE_QUOTE = "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
	protected Class<?> entityClass;
	protected String fileLocation;
	protected List<T> records = new ArrayList<T>();
	protected List<T> filteringRecords = new ArrayList<T>();
	protected List<T> resultRecords = new ArrayList<T>();
	protected String[] columnNames;
	protected String[] columnNamesInCSV;
	private boolean andWhere = false;
	private boolean orWhere = false;
	
	protected GenericModel (Class<?> entityClass, String fileLocation){
		this.entityClass = entityClass;
		this.fileLocation = fileLocation;
	}
	
	public GenericModel (Class<?> entityClass, List<T> objects){
		this.entityClass = entityClass;
		this.records = objects;
	}
	
	// Override by CSVModel and SERModel
	public Model<T> startMapping(){
		return this;
	}
	
	protected boolean CheckAnnotationConstraints(T t){
		boolean isAllPassed = false;
		int primaryKeyCount = 0;
		int fieldPassedOverAnnotationTest = 0;
		Field[] fields = t.getClass().getDeclaredFields();
		Method[] methods = t.getClass().getDeclaredMethods();
		
		// Check Annotation Constraints
		try {
			for(Field field: fields){
				int annotationPassed = 0;
				Annotation[] annotations = field.getAnnotations();
				Method methodOfField = null;
				
				for(Method method: methods){
					if(method.getName().toLowerCase().equals("get"+field.getName().toLowerCase())){
						methodOfField = method;
					}
				}
				Object value = methodOfField.invoke(t, null);
				for(Annotation annotation: annotations){
					T valueInRecords = null;
					if(annotation instanceof PrimaryKey){
						if(!records.isEmpty()){
							valueInRecords = where(field.getName(), Operator.EQUALS, String.valueOf(value)).toSingle();
						}
						
						filteringRecords = new ArrayList<T>(); //because where is called inside this class
						if(records.contains(valueInRecords)){
							ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Duplicate entry, Field ["+field.getName()+" (@PrimaryKey) = "+value+"] has found in the data. ");
						} else {
							if(value != null && value != ""){
								annotationPassed++;
							}
						}
						primaryKeyCount++;
						
					}
					if(annotation instanceof NotNull){
						if(value != null && !value.equals("")){
							annotationPassed++;
						} else {
							ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Field ["+field.getName()+" (@NotNull) = "+value+"] Can't be null or empty.");
						}
					}
					if(annotation instanceof Unique){
						if(!records.isEmpty()){
							valueInRecords = where(field.getName(), Operator.EQUALS, String.valueOf(value)).toSingle();
						}
						filteringRecords = new ArrayList<T>(); //because where is called inside this class
						if(records.contains(valueInRecords)){
							ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Duplicate entry, Field ["+field.getName()+" (@Unique) = "+value+"] has found in the data.");
						} else {
							annotationPassed++;
						}
					}
				}
				if(primaryKeyCount > 1) {
					ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Multiple @PrimaryKey detected in "+entityClass.getSimpleName()+".class. @PrimaryKey should be defined once.");
				}
				if(annotationPassed == annotations.length){
					fieldPassedOverAnnotationTest++;
				}
			}
			if(fieldPassedOverAnnotationTest == fields.length && primaryKeyCount == 1){
				isAllPassed = true;
			}
		} catch (IllegalAccessException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		} catch (IllegalArgumentException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		} catch (InvocationTargetException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		} catch (SecurityException e) {
			ErrorsHelper.addSuppressedAndPrintStackTree(e);
		}
		filteringRecords = new ArrayList<T>();
		resultRecords = new ArrayList<T>();
		
		return isAllPassed;
	}
	
	public void insert(T t){
		if(CheckAnnotationConstraints(t)){
			records.add(t);
		} else {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Data can't be inserted.");
		}
	}
	
	public void update(T oldElement, T newElement){
		if(CheckAnnotationConstraints(newElement)){
			Collections.replaceAll(records, oldElement, newElement);
		} else {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Data can't be updated.");
		}
	}
	
	public void delete(){
		records.removeAll(resultRecords);
		filteringRecords = new ArrayList<T>();
		resultRecords = new ArrayList<T>();
	}
	
	public void delete(T t){
		records.remove(t);
		filteringRecords = new ArrayList<T>();
		resultRecords = new ArrayList<T>();
	}

	public Model<T> where(T t) {
		if(filteringRecords.isEmpty() || orWhere){
			filteringRecords = records;
		}
		
		if(andWhere){
			filteringRecords = resultRecords;
			resultRecords = new ArrayList<T>();
		}
		if(!orWhere){
			resultRecords = new ArrayList<T>();
		}
		
		for(T object: filteringRecords){
			Method[] methods = object.getClass().getDeclaredMethods();
			int objectsCount = 0;
			int objectEqualCount = 0; // Count not_null object of t
			for(Method method: methods){
				if(method.getName().contains("get")){
					try {
						Object tValue = t.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(t, null);
						Object objectValue = method.invoke(object, null);
						if(tValue != null){
							if(tValue.equals(objectValue)){
								objectEqualCount++;
							}
						} else {
							objectEqualCount++;
						}
					} catch (IllegalAccessException e) {
						ErrorsHelper.addSuppressedAndPrintStackTree(e);
					} catch (IllegalArgumentException e) {
						ErrorsHelper.addSuppressedAndPrintStackTree(e);
					} catch (InvocationTargetException e) {
						ErrorsHelper.addSuppressedAndPrintStackTree(e);
					} catch (NoSuchMethodException e) {
						ErrorsHelper.addSuppressedAndPrintStackTree(e);
					} catch (SecurityException e) {
						ErrorsHelper.addSuppressedAndPrintStackTree(e);
					}
					objectsCount++;
				}
			}
			if(objectsCount==objectEqualCount){
				if(!resultRecords.contains(object)){
					resultRecords.add(object);
				}
			}
		}
		
		return this;
	}
	
	public Model<T> where(String columnName, Model.Operator operator, String value) {
		Method[] methods = entityClass.getDeclaredMethods();
		Integer indexOfWantedMethod = null;
		Class<?>[] parameterType = null;
		
		for(int i=0; i<methods.length;i++){
			if(methods[i].getName().equalsIgnoreCase("get"+columnName)){
				indexOfWantedMethod = i;
				parameterType = methods[i].getParameterTypes();
			}
		}
		if(indexOfWantedMethod != null){
			if(filteringRecords.isEmpty()){
				filteringRecords = records;
			}
			
			if(andWhere){
				filteringRecords = resultRecords;
				resultRecords = new ArrayList<T>();
			}
			if(!orWhere){
				resultRecords = new ArrayList<T>();
			}
			
			for(T r: filteringRecords){
				try {
					Method method = r.getClass().getMethod(methods[indexOfWantedMethod].getName(), parameterType);
					String valueOfInvokedMethod = String.valueOf(method.invoke(r, null));
					
					if(operator.equals(Operator.CONTAINS) && valueOfInvokedMethod.toLowerCase().contains(value.toLowerCase())){
						if(!resultRecords.contains(r)){
							resultRecords.add(r);
						}
					} else if(operator.equals(Operator.NOT_CONTAINS) && !valueOfInvokedMethod.toLowerCase().contains(value.toLowerCase())){
						if(!resultRecords.contains(r) || resultRecords.isEmpty()){
							resultRecords.add(r);
						}
					} else if(operator.equals(Operator.EQUALS) && valueOfInvokedMethod.equals(value)){
						if(!resultRecords.contains(r) || resultRecords.isEmpty()){
							resultRecords.add(r);
						}
					} else if(operator.equals(Operator.NOT_EQUALS) && !valueOfInvokedMethod.equals(value)){
						if(!resultRecords.contains(r) || resultRecords.isEmpty()){
							resultRecords.add(r);
						}
					}  else if(operator.equals(Operator.EQUALS_IGNORE_CASE) && valueOfInvokedMethod.toLowerCase().equals(value.toLowerCase())){
						if(!resultRecords.contains(r) || resultRecords.isEmpty()){
							resultRecords.add(r);
						}
					} else if(operator.equals(Operator.NOT_EQUALS_IGNORE_CASE) && !valueOfInvokedMethod.toLowerCase().equals(value.toLowerCase())){
						if(!resultRecords.contains(r) || resultRecords.isEmpty()){
							resultRecords.add(r);
						}
					} else if(operator.equals(Operator.BETWEEN) || operator.equals(Operator.NOT_BETWEEN)){
						String[] values = value.toLowerCase().split("and");
						Double value1 = Double.parseDouble(values[0].trim());
						Double value2 = Double.parseDouble(values[1].trim());
						Double valCompare = Double.parseDouble(valueOfInvokedMethod);
						
						if(operator.equals(Operator.BETWEEN) && (valCompare >= value1 && valCompare <= value2)){
							if(!resultRecords.contains(r) || resultRecords.isEmpty()){
								resultRecords.add(r);
							}
						} else if(operator.equals(Operator.NOT_BETWEEN) && (valCompare <= value1 || valCompare >= value2)) {
							if(!resultRecords.contains(r) || resultRecords.isEmpty()){
								resultRecords.add(r);
							}
						}
					} else if(operator.equals(Operator.IN) || operator.equals(Operator.NOT_IN)){
						String[] input = value.split(","+REGEX_ESCAPE_QUOTE);
						if(operator.equals(Operator.IN) && Arrays.asList(input).contains(valueOfInvokedMethod)){
							if(!resultRecords.contains(r)){
								resultRecords.add(r);
							}
						} else if(operator.equals(Operator.NOT_IN) && !Arrays.asList(input).contains(valueOfInvokedMethod)){
							if(!resultRecords.contains(r)){
								resultRecords.add(r);
							}
						}
					}
				} catch (NoSuchMethodException e) {
					ErrorsHelper.addSuppressedAndPrintStackTree(e);
				} catch (SecurityException e) {
					ErrorsHelper.addSuppressedAndPrintStackTree(e);
				} catch (IllegalAccessException e) {
					ErrorsHelper.addSuppressedAndPrintStackTree(e);
				} catch (IllegalArgumentException e) {
					ErrorsHelper.addSuppressedAndPrintStackTree(e);
				} catch (InvocationTargetException e) {
					ErrorsHelper.addSuppressedAndPrintStackTree(e);
				}
			}
		} else {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] columName not found.");
		}
		return this;
	}

	public Model<T> orWhere(T t) {
		orWhere = true;
		where(t);
		orWhere = false;
		return this;
	}
	public Model<T> orWhere(String columnName, Model.Operator operator, String value) {
		orWhere = true;
		where(columnName, operator, value);
		orWhere = false;
		return this;
	}
	
	public Model<T> andWhere(T t) {
		andWhere = true;
		where(t);
		andWhere = false;
		return this;
	}

	public Model<T> andWhere(String columnName, Model.Operator operator, String value) {
		andWhere = true;
		where(columnName, operator, value);
		andWhere = false;
		return this;
	}

	public Model<T> limit(int min) {
		limit(0, min);
		return this;
	}

	public Model<T> limit(int min, int max) {
		List<T> limitedRecord = new ArrayList<T>();
		if(resultRecords.isEmpty()){
			resultRecords = filteringRecords;
		}
		if(min <= 0){
			min = 1;
		}
		if(max < 0){
			max = 1;
		}
		if(max > resultRecords.size()){
			max = resultRecords.size();
		}
		for(int i=min-1;i<max;i++){
			limitedRecord.add(resultRecords.get(i));
		}
		resultRecords = limitedRecord;
		return this;
	}

	public List<T> toList() {
		if(records.isEmpty()){
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Records is Empty. Have you call startMapping() method?");
			return null;
		} else if(resultRecords.isEmpty() && filteringRecords.isEmpty()){
			resultRecords = records;
		}
		
		if(resultRecords.isEmpty()){
			return null;
		} else {
			return resultRecords;
		}
	}

	public T toSingle() {
		if(records.isEmpty()){
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] Records is Empty. Have you call startMapping() method?");
			return null;
		} else if(resultRecords.isEmpty() && filteringRecords.isEmpty()){
			resultRecords = records;
		}
		
		if(resultRecords.isEmpty()){
			return null;
		} else {
			return resultRecords.get(0);
		}
	}

	// Override by CSVModel and SERModel
	public void save() {
		ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] try call exportToCSV() or exportToSER() instead.");
	}
	
	public void exportToCSV(String fileLocation){
		try {
			this.fileLocation = fileLocation;
			PrintWriter pw = new PrintWriter(new FileWriter(new File(Helper.filePath(fileLocation)+Helper.fileName(fileLocation)+((Helper.fileExtension(fileLocation) == "") ? "" : ".csv"))));
			String line = "";
			for(int i=0;i<columnNamesInCSV.length;i++){
				line = line +columnNamesInCSV[i];
				if(i!=columnNamesInCSV.length-1){
					line = line + ",";
				}
			}
			pw.println(line); //Write columnNames
			for(T r: records){
				line = "";
				for(int i=0; i<columnNames.length;i++){ // Write according to column's sorting, reflection method not consistently ordered as declared
					for(Method m: r.getClass().getDeclaredMethods()){
						if(m.getName().toLowerCase().equals("get"+columnNames[i])){
							try {
								String invokedValue = String.valueOf(m.invoke(r, null));
								if(invokedValue.contains(",")){
									invokedValue = "\"" + invokedValue + "\"";
								}
								line = line + invokedValue;
								if(i != columnNames.length-1){
									line = line + ",";
								}
							} catch (IllegalAccessException e) {
								ErrorsHelper.addSuppressedAndPrintStackTree(e);
							} catch (IllegalArgumentException e) {
								ErrorsHelper.addSuppressedAndPrintStackTree(e);
							} catch (InvocationTargetException e) {
								ErrorsHelper.addSuppressedAndPrintStackTree(e);
							}
						}
					}
				}
				pw.println(line); //Write Data
			}
			pw.flush();
			pw.close();
		} catch (IOException e) {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] File Not Found.");
		}
	}
	
	public void exportToSER(String fileLocation){
		this.fileLocation = fileLocation;
		File serFile = new File(Helper.filePath(fileLocation)+Helper.fileName(fileLocation)+((Helper.fileExtension(fileLocation) == "") ? "" : ".ser"));
		try {
			FileOutputStream fout = new FileOutputStream(serFile);
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(records);
			out.close();
			fout.close();
		} catch (NotSerializableException e){
			serFile.delete();
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] " + entityClass.getName()+" is not implementing java.io.Serializable");
		} catch (FileNotFoundException e) {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] File "+Helper.fileName(fileLocation)+".ser can't be created. Access denied.");
		} catch (IOException e) {
			ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] File "+Helper.fileName(fileLocation)+".ser can't be accessed.");
		}
	}
}
