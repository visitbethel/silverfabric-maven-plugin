package com.tibco.silverfabric.components;

import java.io.File;

public class Utils {

	/**
	 * 
	 * @param c
	 * @param number
	 * @param ext
	 * @return
	 */
	public static File getTestFile(Class c, int number, String ext) {
		return new File(String.format("src/test/resources/components/%s%s.%s",
				c.getSimpleName(), number, ext));
	}

}
