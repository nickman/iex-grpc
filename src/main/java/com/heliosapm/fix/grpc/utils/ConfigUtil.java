package com.heliosapm.fix.grpc.utils;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: ConfigUtil</p>
 * <p>Description: Configuration utilities</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.utilsConfigUtil</code></p>
 * <p>2019</p>
 */
public class ConfigUtil {
	/** The env/system prop pattern */
	public static final Pattern TOKEN = Pattern.compile("\\$\\{(.*?)\\}");
	/**
	 * Merges all the passed properties into one
	 * @param properties The properties to merge
	 * @return the merged properties
	 */
	public static Properties mergeProperties(Properties...properties) {
		Properties allProps = new Properties();
		if(properties==null || properties.length==0) {
			allProps.putAll(System.getProperties());
		} else {
			for(int i = properties.length-1; i>=0; i--) {
				if(properties[i] != null && properties[i].size() >0) {
					allProps.putAll(properties[i]);
				}
			}
		}
		return allProps;
	}
	
	/**
	 * Substitutes all instances of <b><code>${XX}</code></b> tokens in a string where <b><code>XX</code></b> is an environmental variable or a system property name.  
	 * @param template The string to substitute
	 * @param envThenSystem If true, evaluates the environment first, then system properties. If false, then the reverse.
	 * @return the substituted string.
	 */
	public static String tokenFillIn(CharSequence template, boolean envThenSystem, String[]...replaces) {
		boolean anyMatches = false;
		StringBuffer repl = new StringBuffer();
		Matcher matcher = TOKEN.matcher(Objects.requireNonNull(template, "Input template was null"));
		while(matcher.find()) {
			anyMatches = true;
			String configValue = null;
			if(envThenSystem) {
				configValue = getEnvThenSystemProperty(matcher.group(1), matcher.group(1));				
			} else {
				configValue = getSystemThenEnvProperty(matcher.group(1), matcher.group(1));
			}		
			if(replaces!=null && replaces.length > 0) {
				for(String[] replace: replaces) {
					if(replace.length>1) {
						try {
							configValue = configValue.replace(replace[0], replace[1]);
						} catch (Exception e) {};
					}
				}
			}
			matcher.appendReplacement(repl, configValue);
		}
		if(anyMatches) {
			matcher.appendTail(repl);
			return repl.toString();
		} else {
			return template.toString();
		}
	}
	
	/**
	 * Looks up a property, first in the environment, then the system properties. 
	 * If not found in either, returns the supplied default.
	 * @param name The name of the key to look up.
	 * @param defaultValue The default to return if the name is not found.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located value or the default if it was not found.
	 */
	public static String getEnvThenSystemProperty(String name, String defaultValue, Properties...properties) {
		
		String value = System.getenv(systemToEnv(name));
		if(value==null) {			
			value = mergeProperties(properties).getProperty(name);
		}
		if(value==null) {
			value=defaultValue;
		}
		return value;
	}
	
	/**
	 * Converts a system property key to an env variable key
	 * @param key The key to convert
	 * @return the env var key
	 */
	public static String systemToEnv(String key) {
		return Objects.requireNonNull(key, "Passed key was null")
			.trim().toUpperCase().replace('.', '_');
	}
	
	/**
	 * Looks up a property, first in the system properties, then the environment. 
	 * If not found in either, returns the supplied default.
	 * @param name The name of the key to look up.
	 * @param defaultValue The default to return if the name is not found.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located value or the default if it was not found.
	 */
	public static String getSystemThenEnvProperty(String name, String defaultValue, Properties...properties) {
		String value = mergeProperties(properties).getProperty(name);
		if(value==null) {
			value = System.getenv(systemToEnv(name));
		}
		if(value==null) {
			value=defaultValue;
		}
		return value;
	}	
	
	/**
	 * Determines if a name has been defined in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined in the environment or system properties.
	 */
	public static boolean isDefined(String name, Properties...properties) {
		if(System.getenv(name) != null) return true;
		if(mergeProperties(properties).getProperty(name) != null) return true;
		return false;		
	}
	
	/**
	 * Determines if a name has been defined as a valid int in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined as a valid int in the environment or system properties.
	 */
	public static boolean isIntDefined(String name, Properties...properties) {
		String tmp = getEnvThenSystemProperty(name, null, properties);
		if(tmp==null) return false;
		try {
			Integer.parseInt(tmp);
			return true;
		} catch (Exception e) {
			return false;
		}				
	}
	
	/**
	 * Determines if a name has been defined as a valid boolean in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined as a valid boolean in the environment or system properties.
	 */
	public static boolean isBooleanDefined(String name, Properties...properties) {
		String tmp = getEnvThenSystemProperty(name, null, properties);
		if(tmp==null) return false;
		try {
			tmp = tmp.toUpperCase();
			if(
					tmp.equalsIgnoreCase("TRUE") || tmp.equalsIgnoreCase("Y") || tmp.equalsIgnoreCase("YES") ||
					tmp.equalsIgnoreCase("FALSE") || tmp.equalsIgnoreCase("N") || tmp.equalsIgnoreCase("NO")
			) return true;
			else return false;
		} catch (Exception e) {
			return false;
		}				
	}	
	
	/**
	 * Determines if a name has been defined as a valid long in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined as a valid long in the environment or system properties.
	 */
	public static boolean isLongDefined(String name, Properties...properties) {
		String tmp = getEnvThenSystemProperty(name, null, properties);
		if(tmp==null) return false;
		try {
			Long.parseLong(tmp);
			return true;
		} catch (Exception e) {
			return false;
		}				
	}
	
	/**
	 * Returns the value defined as an Integer looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid int.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located integer or the passed default value.
	 */
	public static int getIntSystemThenEnvProperty(String name, int defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		try {
			return Integer.parseInt(tmp);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * Returns the value defined as an Float looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid int.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located float or the passed default value.
	 */
	public static Float getFloatSystemThenEnvProperty(String name, Float defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		try {
			return Float.parseFloat(tmp);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	
	/**
	 * Returns the value defined as a Long looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid long.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located long or the passed default value.
	 */
	public static long getLongSystemThenEnvProperty(String name, long defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		try {
			return Long.parseLong(tmp);
		} catch (Exception e) {
			return defaultValue;
		}
	}	
	
	/**
	 * Returns the value defined as a Boolean looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid boolean.
	 * @param propeties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located boolean or the passed default value.
	 */
	public static boolean getBooleanSystemThenEnvProperty(String name, boolean defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		if(tmp==null) return defaultValue;
		tmp = tmp.toUpperCase();
		if(tmp.equalsIgnoreCase("TRUE") || tmp.equalsIgnoreCase("Y") || tmp.equalsIgnoreCase("YES")) return true;
		if(tmp.equalsIgnoreCase("FALSE") || tmp.equalsIgnoreCase("N") || tmp.equalsIgnoreCase("NO")) return false;
		return defaultValue;
	}		

}
