package com.latte.orm;
/**
 * @author hikmatullohhari
 */
import java.util.List;

public interface Model<T> {
	public static enum Operator {
		CONTAINS, NOT_CONTAINS, EQUALS, NOT_EQUALS, EQUALS_IGNORE_CASE, NOT_EQUALS_IGNORE_CASE, BETWEEN, NOT_BETWEEN, IN, NOT_IN
	};
	// [CONTAINS, NOT_CONTAINS, EQUALS, NOT_EQUALS] format: "value" (value can be String or Numeric)
	// [BETWEEN, NOT_BETWEEN] format: "value2 and value2" (values should be type Numeric)
	// [IN, NOT_IN] format: "value1,value2,value3,value_n" (values can be String or Numeric)
	
	public Model<T> startMapping();
	public void insert(T t);
	public void update(T oldElement, T newElement);
	public void delete(T t);
	public void delete();
	public Model<T> where(T t);
	public Model<T> where(String columnName, Operator Operator, String value);
	public Model<T> orWhere(T t);
	public Model<T> orWhere(String columnName, Operator Operator, String value);
	public Model<T> andWhere(T t);
	public Model<T> andWhere(String columnName, Operator Operator, String value);
	public Model<T> limit(int min);
	public Model<T> limit(int min, int max);
	public List<T> toList();
	public T toSingle();
	public void save();
	public void exportToCSV(String fileLocation);
	public void exportToSER(String fileLocation);
}
