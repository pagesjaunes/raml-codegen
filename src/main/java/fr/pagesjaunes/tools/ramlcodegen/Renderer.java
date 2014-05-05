package fr.pagesjaunes.tools.ramlcodegen;

import java.util.Map;

import org.raml.model.Raml;

public interface Renderer {
	
	public Bundle render(Raml aRoot, Map<String, String> someOpts);

}
