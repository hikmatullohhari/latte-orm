package com.latte.orm.helpers;
/**
 * @author hikmatullohhari
 */
import java.lang.reflect.Method;

public class ReflectionHelper {
	//Auto cast the type of value gotten from method
	public static Object autocast(Method method, Object rawValue){
		Class<?> p = method.getParameterTypes()[0];
		Object value = null;
		if(p.isPrimitive()){
			if(p.getName().equals("int")){
				value = Integer.parseInt((String) rawValue);
			} else if (p.getName().equals("float")) {
				value = Float.parseFloat((String) rawValue);
			} else if (p.getName().equals("double")) {
				value = Double.parseDouble((String) rawValue);
			}
		} else {
			String cl = method.getParameterTypes()[0].getName();
			Class<?> theClass;
			try {
				theClass = Class.forName(cl);
				value = theClass.cast(rawValue);
			} catch (ClassNotFoundException e) {
				ErrorsHelper.setErrorMessageAndPrintToConsole("[Error] "+ e.getMessage());
			}
		}
		return value;
	}
}
