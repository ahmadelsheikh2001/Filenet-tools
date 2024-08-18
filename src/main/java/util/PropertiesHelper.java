package util;

 
import java.io.File;
import java.io.PrintStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertiesHelper {

	private static PropertiesConfiguration configuration;
	private static PropertiesHelper instance_c;
 	private static final String ISO = null;

	/**
	 * Protected Constructor. Only on instance of this class can ever be
	 * Instantiated.
	 * @param filePath 
	 * 
	 * @throws ConfigurationException
	 *             Thrown if properties file can't be read.
	 */
	protected PropertiesHelper(String filePath) throws ConfigurationException {
        CustomLogging.log(" Getting input stream for Application.properties file...");
		try {
		configuration = new PropertiesConfiguration(new File(filePath));
		byte ptext[] = ":اللغة العربية".getBytes("ISO-8859-1"); 
		String value = new String(ptext, "UTF-8"); 		
		final PrintStream out = new PrintStream(System.out, true, "UTF-8");
 		
		
		System.out.println("PropertiesHelper----------------------->>>>>>>>>>>>>>>>>>>>");
		}catch(Exception e) {
			CustomLogging.log("An exception "+e.getMessage());
			System.out.println("Exception---------------------------->>>>>>>>>>>>>>>>>>>>");
			System.out.println(e);
 		}
 	}

	/**
	 * Returns the instance of the class. Creates and instance if one doesn't
	 * already exist.
	 * 
	 * @return Instance of PropertiesHelper
	 * @throws ConfigurationException
	 *             Thrown if properties file can't be read.
	 */
	public static PropertiesHelper getInstance() {
		return getInstance("Application.properties");
	}
	public static PropertiesHelper getInstances() {
		return getInstancee("Application.properties");
	}
	public static PropertiesHelper getInstance(String filePath) {
		//LOG.info("Getting instance of properties class...");
		if (instance_c == null) {
		//	LOG.info("No existing instance, Creating new instance...");
			try {
				instance_c = new PropertiesHelper(filePath);
			} catch (ConfigurationException e) {
				//LOG.error("Configuration not loaded", e);
			}
		}
		//LOG.info("Returning instance of properties class...");
		return instance_c;
	}

	public static PropertiesHelper getInstancee(String filePath) {
		//LOG.info("Getting instance of properties class...");
		
			//LOG.info("No existing instance, Creating new instance...");
			try {
				instance_c = new PropertiesHelper(filePath);
			} catch (ConfigurationException e) {
				//LOG.error("Configuration not loaded", e);
			}
		
		//LOG.info("Returning instance of properties class...");
		return instance_c;
	}
	
	
	/**
	 * Returns the associated value of the properties key.
	 * 
	 * @param key_p
	 *            Key of property to retrieve.
	 * @return Value of property.
	 */
	public String getProperty(final String key_p) {
		//LOG.info("Getting value for properties: " + key_p + "...");
		//System.out.println("Getting value for properties: " + key_p + "...");
		String value = configuration.getString(key_p);
		if(value!=null){
			value=value.trim();
		}
		return value;
		 
	}
//	public String[] securityCheck() {
//		String value = configuration.getString("securityToken");
//		if(value!=null){
//			value=value.trim();
//		}
//		String seperator="\\*S\\*";
//		String [] splitValue= value.split(seperator);
//		splitValue[0] = PasswordHandelling.decrypt(splitValue[0]);   
//		splitValue[1] = PasswordHandelling.decrypt(splitValue[1]);   
//		//System.out.println(splitValue[0]+" "+splitValue[1]);
//		return splitValue;
//	}
	
	public String[] getProperties(final String key_p) {
 		System.out.println("Getting value for properties: " + key_p + "...");
		String value[] = configuration.getStringArray(key_p);
		//System.out.println("Returning value: '" + value + "' - for key: " + key_p);
		return value;
	}

	public void saveProperty(String key, String value) {
 		System.out.println("Saving value for property: " + key + "...");
		configuration.setProperty(key, value);
 		//System.out.println("value: '" + value + "' - for key: " + key + " was saved");
	}

	public void saveToFile()  {
		//LOG.info("Saving changes to the properties file ...");
		try {
			configuration.save();
		} catch (ConfigurationException e) {
			CustomLogging.log("An error occured or no file name has been set yet" + e.getMessage());
			//LOG.error("An error occured or no file name has been set yet", e);
		}
	}
}
