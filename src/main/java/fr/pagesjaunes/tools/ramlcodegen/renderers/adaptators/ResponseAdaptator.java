package fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators;

import java.util.HashMap;
import java.util.Map;

import org.raml.model.MimeType;
import org.raml.model.Response;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

public class ResponseAdaptator extends ObjectModelAdaptor {

	public ResponseAdaptator() {
		// Nothing to do
	}

	@Override
	public synchronized Object getProperty(Interpreter anInter, ST aSt,
			Object anObject, Object aProperty, String aName)
			throws STNoSuchPropertyException {
		Response oResponse=(Response)anObject;
		if(aName.equals("body")) {
			if(oResponse.getBody()==null || oResponse.getBody().size()==0) {
				Map<String, MimeType> oMap=oResponse.getBody()==null?new HashMap<String, MimeType>():oResponse.getBody();
				oMap.put("*/*", new MimeType("*/*"));
				oResponse.setBody(oMap);
			}
		}
		return super.getProperty(anInter, aSt, anObject, aProperty, aName);
	}
}
