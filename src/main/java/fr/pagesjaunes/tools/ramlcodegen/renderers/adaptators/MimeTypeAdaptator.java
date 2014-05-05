package fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.raml.model.MimeType;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

public class MimeTypeAdaptator extends ObjectModelAdaptor {

	public MimeTypeAdaptator() {
		// Nothing to do
	}

	@Override
	public synchronized Object getProperty(Interpreter anInter, ST aSt,
			Object anObject, Object aProperty, String aName)
			throws STNoSuchPropertyException {
		MimeType oBody=(MimeType)anObject;
		if(aName.equals("isSpecific")) {
			return !oBody.getType().equals("*/*");
		}
		if(aName.equals("name")) {
			return StringUtils.replaceChars(oBody.getType(), "*/", "s_");
		}
		if(aName.equals("schemaEscape")) {
			return StringEscapeUtils.escapeJava(oBody.getSchema());
		}
		if(aName.equals("exampleEscape")) {
			return StringEscapeUtils.escapeJava(oBody.getExample());
		}
		if(aName.equals("exampleEscapeJS")) {
			return StringEscapeUtils.escapeEcmaScript(oBody.getExample());
		}
		return super.getProperty(anInter, aSt, anObject, aProperty, aName);
	}
}
