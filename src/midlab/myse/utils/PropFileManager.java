package midlab.myse.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The manager of the properties file in the classpath
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class PropFileManager {
	
	/** The Properties object **/
	Properties prop;
	
	/**
	 * Build a PropFileManager given a properties file name
	 * @param propFileName the file name of properties file
	 * @throws IOException
	 */
	public PropFileManager(String propFileName) throws IOException{
		Properties prop = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		this.prop = prop;
	}
	/**
	 * Given a properties file name in the classpath, return an object Properties
	 * @param propFileName the properties file name
	 * @return the Properties object
	 * @throws IOException
	 */
	public Properties getProperties(){
		return this.prop;
	}
	
	/**
	 * Given an object Properties and an attribute name, return the value of that attribute even in presence of
	 * variable written in the properties file. The method identified a variable with a String between '$'
	 * (e.g. $path$).
	 * @param propAttribute the attribute name we want to discover the value
	 * @return the value of the attribute propAttribute
	 */
	public String getProperty(String propAttribute){
		String value = prop.getProperty(propAttribute);
		String subVarName = "";
		String subVarValue = "";
		int start = 0;
		int index = 0;
		int end = 0;
		
		// while a variable there exists...
		while(value.indexOf("$")!=-1){
			
			// set the start index of a variable, go on till find the end index of that variable
			start = value.indexOf("$");
			index = start+1;
			while(value.charAt(index)!='$'){
				++index;
			}
			end = index+1;
			
			// found an internal variable, let's find out its value
			subVarName = value.substring(start, end);
			subVarValue = prop.getProperty(value.substring(start+1, end-1));
	
			// set the new value with the substitution of a variable (subVarName) with its value (subVarValue)
			value = value.replace(subVarName, subVarValue);
			
			// reset the indexes
			start = end = index = 0;
		}
		return value;
	}
	
}
