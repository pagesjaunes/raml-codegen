package fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.pagesjaunes.tools.ramlcodegen.Utils;

public class Resolver {
	
	private static final Logger logger = LoggerFactory.getLogger(Resolver.class);
	
	public static String getValueParam(AbstractParam aParam, String aName, String aWarning) {
		if(aParam.getExample()!=null) {
			return aParam.getExample();
		}
		if(aParam.getDefaultValue()!=null) {
			return aParam.getDefaultValue();
		}
		if(!aParam.getEnumeration().isEmpty()) {
			return aParam.getEnumeration().get(0);
		}
		logger.warn(aWarning + "- No exemple or default value for: " + aName);
		return aName;
	}
			
	public static <E extends AbstractParam> String resolve(String aUrl, Map<String,E> aParams, String aLogHeader) {
		Map<String, String> oMap=new HashMap<String, String>();
		for(Map.Entry<String, E> oE:aParams.entrySet()) {
			String oValue=getValueParam(oE.getValue(), oE.getKey(), aLogHeader);
			oMap.put(oE.getKey(), Utils.encode(oValue));
		}
		return Utils.resolve(aUrl, oMap);
	}
    
    public static Map<String, UriParameter> fromBaseToUri(Map<String, List<UriParameter>> aMap) {
    	Map<String, UriParameter> oParams=new HashMap<String, UriParameter>();
    	for (Map.Entry<String, List<UriParameter>> oE:aMap.entrySet()) {
			oParams.put(oE.getKey(), oE.getValue().get(0));
		}
    	return oParams;
    }
}
