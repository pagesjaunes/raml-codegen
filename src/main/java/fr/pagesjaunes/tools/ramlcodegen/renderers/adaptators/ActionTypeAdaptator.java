package fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators;

import org.raml.model.ActionType;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

public class ActionTypeAdaptator extends ObjectModelAdaptor {

	public ActionTypeAdaptator() {
		// Nothing to do
	}

	@Override
	public synchronized Object getProperty(Interpreter anInter, ST aSt,
			Object anObject, Object aProperty, String aName)
			throws STNoSuchPropertyException {
		ActionType oType=(ActionType)anObject;
		if(aName.equals("validation")) {
			return "validation"+oType;
		}
		return super.getProperty(anInter, aSt, anObject, aProperty, aName);
	}
}
