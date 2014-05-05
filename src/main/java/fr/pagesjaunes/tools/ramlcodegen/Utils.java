package fr.pagesjaunes.tools.ramlcodegen;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Utils {
	public static String cleanString(String aString) {
		return StringUtils.replaceChars(StringUtils.strip(aString), " -+;./,^!(){}?\"'","________________");
	}
	
	public static String resolve(String aTemplate, String aVarName, String aValue) {
		return StringUtils.replace(aTemplate, "{"+aVarName+"}", aValue);
	}
	
	public static String resolve(String aTemplate, Map<String, String> aMap) {
		for(Map.Entry<String, String> oE:aMap.entrySet()) {
			aTemplate=resolve(aTemplate, oE.getKey(), oE.getValue());
		}
		return aTemplate;
	}
	
	public static String encode(String aValue) {
		try {
			return URLEncoder.encode(aValue, "UTF8");
		} catch (UnsupportedEncodingException e) {
			return aValue;
		}
	}
	
	public static boolean hasBeenResolved(String aValue) {
		return !StringUtils.contains(aValue, '{');
	}
}
