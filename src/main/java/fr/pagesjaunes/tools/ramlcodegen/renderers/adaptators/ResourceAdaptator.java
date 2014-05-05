package fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators;

import org.apache.commons.lang3.StringUtils;
import org.raml.model.ActionType;
import org.raml.model.Resource;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import fr.pagesjaunes.tools.ramlcodegen.Utils;

public class ResourceAdaptator extends ObjectModelAdaptor {

	public ResourceAdaptator() {
		// Nothing to do
	}

	@Override
	public synchronized Object getProperty(Interpreter anInter, ST aSt,
			Object anObject, Object aProperty, String aName)
			throws STNoSuchPropertyException {
		Resource oResource=(Resource)anObject;
		if(aName.startsWith("uri")) {
			String oUri = oResource.getParentResource()==null?"":(String)getProperty(anInter, aSt, oResource.getParentResource(), aName, aName);
			oUri += oResource.getRelativeUri();
			if(aName.endsWith("express")) {
				oUri=StringUtils.replace(oUri, "{mediaTypeExtension}", "");
				oUri=StringUtils.replace(StringUtils.replaceChars(oUri, '{', ':'),"}", "");
			}
			return oUri;
		}
		if(aName.equals("methodUri")) {
			String oReturn=Utils.cleanString(oResource.getRelativeUri());
			for(Resource oR=oResource.getParentResource(); oR!=null; oR=oR.getParentResource()) {
				oReturn=Utils.cleanString(oR.getRelativeUri())+"_"+oReturn;
			}
			return oReturn;
		}
		if(aName.equals("get")) {
			return oResource.getAction(ActionType.GET);
		}
		if(aName.equals("post")) {
			return oResource.getAction(ActionType.POST);		
		}
		if(aName.equals("put")) {
			return oResource.getAction(ActionType.PUT);
		}
		if(aName.equals("delete")) {
			return oResource.getAction(ActionType.DELETE);
		}
		return super.getProperty(anInter, aSt, anObject, aProperty, aName);
	}
}
