package com.tibco.silverfabric;

import java.io.File;

public class Utils {

	public static final String PREFIX_COMPONENT = "__UNIT_COMPONENT__";
	public static final String PREFIX_STACK = "__UNIT_STACK__";
	/**
	 * 
	 * @param c
	 * @param number
	 * @param ext
	 * @return
	 */
	public static File getTestFile(Class c, int number, String ext) {
		return new File(String.format("src/test/resources/%s/%s%s.%s", c
				.getSimpleName().contains("acks") ? "stacks" : "components", c
				.getSimpleName(), number, ext));
	}

	/**
	 * 
	 * @param c
	 * @param number
	 * @param ext
	 * @return
	 */
	public static String getEntityName(Class c, String prefix) {
		String sn = c.getSimpleName();
		return String.format("%s-%s", prefix, sn);
	}
	/**
	 * 
	 * @param c
	 * @param number
	 * @param ext
	 * @return
	 */
	public static String getEntityName(Class c, String prefix, String suffix) {
		String sn = c.getSimpleName();
		return String.format("%s-%s-%s", prefix, sn, suffix);
	}
		
}
