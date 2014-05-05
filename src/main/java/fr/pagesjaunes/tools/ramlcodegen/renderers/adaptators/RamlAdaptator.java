package fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators;

import org.raml.model.Raml;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import fr.pagesjaunes.tools.ramlcodegen.Utils;

public class RamlAdaptator extends ObjectModelAdaptor {

	public RamlAdaptator() {
		// Nothing to do
	}

	@Override
	public synchronized Object getProperty(Interpreter anInter, ST aSt,
			Object anObject, Object aProperty, String aName)
			throws STNoSuchPropertyException {
		Raml oRoot=(Raml)anObject;
		if(aName.equals("baseUri")) {
			return Resolver.resolve(oRoot.getBaseUri(), oRoot.getBaseUriParameters(), "[BaseUriParameter]");
		}
		if(aName.equals("cleanVersion")) {
			if(oRoot.getVersion()!=null) {
				return Utils.cleanString(oRoot.getVersion());
			} else {
				return null;
			}
		}
		return super.getProperty(anInter, aSt, anObject, aProperty, aName);
	}
}
