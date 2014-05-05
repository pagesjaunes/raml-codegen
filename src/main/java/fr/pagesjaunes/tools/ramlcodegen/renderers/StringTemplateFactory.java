package fr.pagesjaunes.tools.ramlcodegen.renderers;

import fr.pagesjaunes.tools.ramlcodegen.Renderer;
import fr.pagesjaunes.tools.ramlcodegen.RendererFactory;

public class StringTemplateFactory implements RendererFactory {

	private String templateName;
	private String[] args;
	
	public StringTemplateFactory(String aTemplateName, String... someArgs) {
		templateName=aTemplateName;
		args=someArgs.clone();
	}
	
	public Renderer create() {
		return new StringTemplateRenderer(templateName, args);
	}
}
