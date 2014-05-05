package fr.pagesjaunes.tools.ramlcodegen.renderers;

import java.util.HashMap;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import fr.pagesjaunes.tools.ramlcodegen.Bundle;
import fr.pagesjaunes.tools.ramlcodegen.Generator;
import fr.pagesjaunes.tools.ramlcodegen.Renderer;
import fr.pagesjaunes.tools.ramlcodegen.Utils;
import fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators.RamlAdaptator;
import fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators.MimeTypeAdaptator;
import fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators.ActionAdaptator;
import fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators.ResourceAdaptator;
import fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators.ResponseAdaptator;

public class StringTemplateRenderer implements Renderer {
	
	private String stgFile;
	private String[] rootTemplateNames;
	
	public StringTemplateRenderer(String aStgFile, String... aRootTemplateNames) {
		stgFile=aStgFile;
		rootTemplateNames=aRootTemplateNames;
	}

	@Override
	public Bundle render(Raml aRoot, Map<String, String> someOpts) {
		STGroup stg = new STGroupFile(stgFile);
		stg.registerModelAdaptor(Resource.class, new ResourceAdaptator());
		stg.registerModelAdaptor(Raml.class, new RamlAdaptator());
		stg.registerModelAdaptor(Action.class, new ActionAdaptator(aRoot));
		stg.registerModelAdaptor(Response.class, new ResponseAdaptator());
		stg.registerModelAdaptor(MimeType.class, new MimeTypeAdaptator());
		Map<String, String> oOpts=new HashMap<String, String>(someOpts);
		oOpts.put("title", Utils.cleanString(aRoot.getTitle()));
		if(someOpts.get("baseUri")!=null) {
			aRoot.setBaseUri(someOpts.get("baseUri"));
		}
		aRoot.setBaseUri(Utils.resolve(aRoot.getBaseUri(), "version", aRoot.getVersion()));
		Bundle oBundle=null;
		ST oRootPath=getST(stg,"rootPath", aRoot, oOpts);
		if(oRootPath!=null) {
			oBundle=new Bundle(oRootPath.render());
		} else {
			oBundle=new Bundle(someOpts.get(Generator.GENERATED_PATH)); 
		}
		for(String oName:rootTemplateNames) {
			ST oContent = getST(stg,oName, aRoot, oOpts);
			ST oFileName = getST(stg,oName+"_file", aRoot,oOpts);
			oBundle.addItem(oFileName.render(), oContent.render());
		}
		return oBundle;
	}
	
	protected ST getST(STGroup aGroup, String aName, Raml aRoot, Map<String, String> someOpts) {
		ST oST = aGroup.getInstanceOf(aName);
		if(oST!=null) {
			oST.add("raml", aRoot);
			oST.add("opts", someOpts);
		}
		return oST;
	}
}
