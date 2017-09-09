# Latte ORM

![Latte-ORM](https://img.shields.io/badge/version-1.0.0-blue.svg?style=flat)
![Latte-ORM](https://img.shields.io/badge/dependencies-none-brightgreen.svg?style=flat)
![Latte-ORM](https://img.shields.io/badge/minimum_java_version_required-1.5-yellow.svg?style=flat)

A Java ORM for mapping CSV file into Java Object then manipulate them using simple command, easy implementation and as similiar as what we usually write when related to database processing.

## Installation

* Via terminal
  ```
   $ mvn install
  ```
	run this command under this project directory and *latte-orm-{version}.jar* will be created in the target folder.
    
* Via Eclipse or Other IDE
  ```
  Open Project -> Run As -> Maven Install
  ```
	Copy *latte-orm-{version}.jar* file to your own project and register it via build path.

## Getting Started


> ### Creating model class

A Model is a representation of the data schema. If you ever play with JDBC, it's called POJO (Plain Old Java Object). For example we have data like this:

| ID | Stock Name      | Price  |
|----|:---------------:| ------:|
| 1  | Sony Xperia Z5  |   $700 |
| 2  | iPhone 7        |  $1320 |
| 3  | Xiaomi Redmi 4X |   $200 |
| 4  | Sony Xperia Z   |   $200 |

Where ***ID*** is the primary key, ***Stock Name*** is unique (can't be the same) and can't be null and the ***Price*** is also can't be null.
We can represent the model into java class like this:
```java

import java.io.Serializable;
import com.latte.orm.annotations.*;

@OnMapping(check=true)
public class Stock implements Serializable{
	@PrimaryKey
	private int id;
	@Unique @NotNull
	private String stockName;
	@NotNull
	private double price;
	
	public Stock(){
		super();
	}
	
	public Stock(int id, String stockName, double price) {
		super();
		this.id = id;
		this.stockName = stockName;
		this.price = price;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getStockName(){
		return this.stockName;
	}
	
	public void setStockName(String stockName){
		this.stockName = stockName;
	}
	
	public double getPrice(){
		return this.price;
	}
	
	public void setPrice(double price){
		this.price = price;
	}

  	@Override
	public String toString() {
    		return "Stock [id=" + id + ", stockName=" + stockName + ", price=" + price + "]";
  	} 
}
```
The column names are representated as variables in the model and the name of method represent the variable must be the same as the variable name (it's case insensitive). If the column name contains space ' ' character, delete the character, a variable can't contains space. If the column names contains any symbol, replace with underscore '_'. And as you notice, four different annotations are there as constraints of the data:
* @OnMapping

This annotation retrieve boolean input on parameter ***check*** as a flag whether the mapping process will check the constraints or not. The declaration of this annotation is optional and the default value *true* will be given if this annotation is not present on the model class. Set to *false* to turn off mapping check constraint. Only declared once in a class model above the class name (annotation for a class).
* @PrimaryKey

This flag will tell that the following variable is the primary key of the model (it is also represent as Unique and NotNull). Only declared once in a class model for one variable/column. 
* @Unique

This flag will tell that the following variable can't be the same for this entire column in the records.
* @NotNull 

This flag will tell that the following variable can't be the same for this entire column in the records.

> ### Instantiate model

Let's say that the stock data is written as csv file located in : ***/Users/latte/Documents/Stock.csv***. We can call CSVModel() to mapping the csv data into java object.
```java
import com.latte.orm.*;
...
String fileLocation = '/Users/latte/Documents/Stock.csv'
Model<Stock> stockModel = new CSVModel<Stock>(Stock.class, fileLocation).startMapping();

//OR if your csv file separated by other than comma ',' character, you can set your own delimiter
Model<Stock> stockModel = new CSVModel<Stock>(Stock.class, fileLocation)
				.delimiter(";")
				.startMapping();
```
If the data is represented in Java Serialization file, we can call SERModel() instead of CSVModel.
```java
import com.latte.orm.*;
...
String fileLocation = '/Users/latte/Documents/Stock.ser'
Model<Stock> stockModel = new SERModel<Stock>(Stock.class, fileLocation).startMapping();
```
Or maybe the data is in Java Object mapped as List<Stock\>, we can call GenericModel() instead

```java
import com.latte.orm.*;
...
// List<Stock> listStock = {let's say it contains data};
Model<Stock> stockModel = new GenericModel<Stock>(Stock.class, listStock);
```
> ### Data selection and Modification

We have these bunch of methods to process the data : 
*insert(), update(), delete(), where(), andWhere(), orWhere(), limit(), toSingle(), toList(), save(), exportToCSV(), exportToSER().*

* **where()**
	* where(columName, operator, value)
	* where(T t) [T is the type generic class, in this case T = Stock ]
    ```java
    //#1 we can do this
    stockModel.where("stockName", Operator.CONTAINS, "Sony");

    //#2 or this
    Stock stockFind = new Stock();
    stockFind.setStockName("Sony Xperia Z5");
    stockModel.where(stockFind);
    
    //#3 or this
    Stock stockFind = new Stock();
    stockFind.setStockName("Sony Xperia Z5");
    stockFind.setPrice(700);
    stockModel.where(stockFind);
    ```
	**Operator available:**
    * CONTAINS
    * NOT_CONTAINS
    * EQUALS
    * NOT_EQUALS
    * EQUALS_IGNORE_CASE
    * NOT_EQUALS_IGNORE_CASE
    * BETWEEN
    * NOT_BETWEEN
    * IN
    * NOT_IN

* **andWhere()**
 	* andWhere(columName, operator, value)
	* andWhere(T t) [T is the type generic class, in this case T = Stock ]
	```java
    //#1 we can do this
    stockModel.where("stockName", Operator.CONTAINS, "Sony")
    	   .andWhere("price", Operator.BETWEEN, "700 and 1500")
	          .andWhere("price", Operator.IN, "700,1500");

    //#2 or this
    Stock stockFind = new Stock();
    stockFind.setPrice(200);
    Stock stockFind2 = new Stock();
    stockFind2.setStockName("Xiaomi Redmi 4X");
    stockModel.where(stockFind)
    	   .andWhere(stockFind2);
    ```

* **orWhere()**
 	* orWhere(columName, operator, value)
	* orWhere(T t) [T is the type generic class, in this case T = Stock ]
	```java
    //#1 we can do this
    stockModel.where("stockName", Operator.CONTAINS, "Sony")
    	   .orWhere("price", Operator.NOT_BETWEEN, "700 and 1500")
	          .orWhere("price", Operator.NOT_IN, "600,800,900");
              
    //#2 or this
    Stock stockFind = new Stock();
    stockFind.setStockName("iPhone 7");
    Stock stockFind2 = new Stock();
    stockFind2.setStockName("Xiaomi Redmi 4X");
    stockModel.where(stockFind)
    	   .orWhere(stockFind2);
    ```


* **insert()**

  insert(T t);
  ```java
  Stock stock1 = new Stock(4, "Sony Xperia Z5 Compact", 650);
  stockModel.insert(stock1);
  ```
* **update()**

  update(T tOld, tNew);
  ```java
  Stock stockOld = stockModel.where("id", Operator.EQUAL, "1").toSingle();
  Stock stockNew = stockModel.where("id", Operator.EQUAL, "1").toSingle();
  stockNew.setStockName("Sony Xperia Z5 Prime");
  stockModel.update(stockOld, stockNew);
  ```
* **delete()**
	* delete(T t)
	```java
    Stock stock1 = stockModel.where("id", Operator.EQUAL, "1").toSingle();
    stockModel.delete(stock1);
    ```
    * delete()
    ```java
    // Delete all data where price equal to 200;
    stockModel.where("price", Operator.EQUAL, 200).delete();
    ```
* **limit()**
	* limit(int max)
	* limit(int min, int max)
  ```java
  //#1
  stockModel.limit(2);
  
  //#2
  stockModel.limit(1,3);
  
  //#3
  stockModel.where("price", Operator.BETWEEN, "200 and 1000").limit(2);
  ```
* **toList()**
  * List<T\> toList();

  Retrieve selected data into List<T\> (List<Stock\>)
  ```java
  List<Stock> listStock =
  		stockModel.where("price", Operator.BETWEEN, "200 and 1000")
  	 		  .limit(2)
			          .toList();
  ```
* **toSingle()**
  * T toSingle();
  
  Retrieve single element of records. If the query process result more than one data, it will get the first element;
  ```java
  Stock stock1 = stockModel.where("id", Operator.EQUAL, "1").toSingle();
  ```
* **save()**
  * void save();
  
  Save the changes made from calling insert(), update(), or delete() to the source of the file (csv or ser) according to the Model called (CSVModel() or SERModel())
  ```java
  //#1
  Stock stock1 = new Stock(4, "Sony Xperia Z5 Compact", 650);
  stockModel.insert(stock1).save();
  
  //#2
  Stock stockOld = stockModel.where("id", Operator.EQUAL, "1").toSingle();
  Stock stockNew = stockModel.where("id", Operator.EQUAL, "1").toSingle();
  stockNew.setStockName("Sony Xperia Z5 Prime");
  stockModel.update(stockOld, stockNew).save();
  
  //#3
  Stock stock1 = stockModel.where("id", Operator.EQUAL, "1").toSingle();
  stockModel.delete(stock1);
  
  ```
* **exportToCSV()**
  * void exportToSER(String fileLocation);
  
  Export saved Java Object into CSV file
  ```java
  String fileLocation = "/Users/latte/Documents/newStock.csv";
  stockModel.exportToCSV(fileLocation);
  
  ```
* **exportToSER()**
  * void exportToSER(String fileLocation);
 
  Export saved Java Object into Java Serialization file
  ```java
  String fileLocation = "/Users/latte/Documents/newStock.ser";
  stockModel.exportToCSV(fileLocation);
  
  ```
  
> *latte orm* © 2017
