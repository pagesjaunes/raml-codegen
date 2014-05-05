package fr.pagesjaunes.tools.ramlcodegen;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.raml.model.Raml;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.pagesjaunes.tools.ramlcodegen.renderers.StringTemplateFactory;

/**
 * Code generator for RAML specifications
 *
 */
public class Generator
{
	// Informations about a factory
	private static class FactoryItem {
		private RendererFactory factory;
		private List<String> documentation;
		
		private FactoryItem(RendererFactory aFactory, String... aDoc) {
			factory=aFactory;
			documentation=Arrays.asList(aDoc);
		}
		
		private RendererFactory getFactory() {
			return factory;
		}
		
		private List<String> getDocumentation() {
			return documentation;
		}
	}
 	
	// Configuration class
	
	public static class Configuration {
		
		private Map<String, FactoryItem> factories=new HashMap<>();
		
		private Configuration() {
		}
		
		private Configuration(Configuration aConfiguration) {
			this();
			factories=new HashMap<>(aConfiguration.factories);
		}
		
		// Configuration management
		private static Configuration createDefaultConfiguration() {
			Configuration oDefaultConfiguration=new Configuration();
			// Register the tests generator factory
			oDefaultConfiguration.register("tests", new StringTemplateFactory("templates/tests-templates/main.stg", 
					"class", 
					"pom", 
					"log4j", 
					"run"), "Generating Rest-Assured unit tests from a RAML specification","baseUri");
			// Register the Robohydra mock generator factory
			oDefaultConfiguration.register("mocks", new StringTemplateFactory("templates/robohydra-templates/main.stg", 
					"config", 
					"plugin",
					"package",
					"custom",
					"verbose"), "Generating a mock Robohydra plugin");
			return oDefaultConfiguration;
		}
		
		public Configuration register(String aName, RendererFactory aFactory, String... aDocumentation) {
			factories.put(aName, new FactoryItem(aFactory,aDocumentation));
			return this;
		}
		
		public Renderer create(String aName) {
			return factories.get(aName).getFactory().create();
		}
		
		public void printHelp(PrintStream aStream) {
			aStream.println("Types and options of the generator");
			aStream.println("----------------------------------");
			for(Map.Entry<String, FactoryItem> oItem:factories.entrySet()) {
				aStream.println();
				aStream.println("Type : "+oItem.getKey());
				List<String> oDoc=oItem.getValue().getDocumentation();
				if(!oDoc.isEmpty()) {
					aStream.println("       "+oDoc.get(0));
					if(oDoc.size()>1) {
						aStream.println("       options:");
						for(int i=1; i<oDoc.size(); i++) {
							aStream.println("               - "+oDoc.get(i));
						}
					}
				}
				aStream.println();
			}
		}
	}
	
	// Default configuration of the generator
	private static Configuration defaultConfiguration=Configuration.createDefaultConfiguration();
	
	// Current configuration
	private Configuration config;
	
	// The configuration of the generator
	public Configuration getConfig() {
		if(config==null) {
			config=new Configuration(defaultConfiguration);
		}
		return config;
	}
	
	// Default configuration of the generator
	public static Configuration config() {
		return defaultConfiguration;
	}
	
	public static final String GENERATED_PATH="generatedPath";
	
	private static final Logger logger = LoggerFactory.getLogger(Generator.class);
	
	private Raml root;
	
	public static Raml parseRaml(String resourceLocation)
    {
        return new RamlDocumentBuilder().build(resourceLocation);
    }
	
	public static List<ValidationResult> validateRaml(String resourceLocation)
    {
        return RamlValidationService.createDefault().validate(resourceLocation);
    }
	
	private Generator(String aResourceLocation) throws GeneratorException {
		super();
		List<ValidationResult> results=validateRaml(aResourceLocation);
		if (results.isEmpty()) {
			root=parseRaml(aResourceLocation);
		} else {
			StringBuilder msg = new StringBuilder("Unexpected errors:\n ");
	        for (ValidationResult vr : results) {
	        	msg.append("\t\t").append(vr.toString()).append("\n");
	        }
	        String oStrMsg=msg.toString();
	        logger.error(oStrMsg);
	        throw new GeneratorException(oStrMsg);
	    }
	}
	
	public static Generator build(String aResourceLocation, boolean aSTflag) {
		try {
			return new Generator(aResourceLocation);
		} catch (GeneratorException oE) {
			if(aSTflag) {
				oE.printStackTrace();
			}
			return null;
		}
	}
	
	public static Generator build(String aResourceLocation) {
		return build(aResourceLocation,false);
	}
	
	public void generate(String aType, String aGeneratePath, Map<String, String> someOpts) 
	{
		Renderer oRenderer=getConfig().create(aType);
		if (oRenderer!=null) {
			Map<String, String> oOpts=someOpts!=null?new HashMap<String, String>(someOpts):new HashMap<String,String>();
			oOpts.put(GENERATED_PATH, aGeneratePath);
			oRenderer.render(root, oOpts).create();	
		}
	}
	
	public void generate(String aType, String aGeneratePath) {
		generate(aType, aGeneratePath,null);
	}
	
	public void generate(String aType)
	{
		generate(aType,".");
	}
	
	private static Options buildOptions() {
		@SuppressWarnings("static-access")
		Option help = OptionBuilder
                .withDescription(  "print this help" )
                .withLongOpt("help")
                .create('h');
		@SuppressWarnings("static-access")
		Option helpType = OptionBuilder
                .withDescription(  "print the specific help types configuration and options" )
                .withLongOpt("help-type")
                .create("ht");
		@SuppressWarnings("static-access")
		Option verbose = OptionBuilder
                .withDescription(  "print full stacktraces when an error occurred" )
                .withLongOpt("verbose")
                .create("v");
		@SuppressWarnings("static-access")
		Option type = OptionBuilder.withArgName( "generationType" )
                .hasArg()
                .withDescription(  "the type of the project which have to be generated" )
                .isRequired(false)
                .withLongOpt("type")
                .create('t');
		@SuppressWarnings("static-access")
		Option source = OptionBuilder.withArgName( "ramlFile url" )
                .hasArg()
                .withDescription(  "the raml file url" )
                .isRequired(false)
                .withLongOpt("source")
                .create('s');
		@SuppressWarnings("static-access")
		Option dest = OptionBuilder.withArgName( "pathName" )
                .hasArg()
                .withDescription(  "the path name of the project which have to be generated" )
                .isRequired(false)
                .withLongOpt("destination")
                .create('d');
		@SuppressWarnings("static-access")
		Option option = OptionBuilder.withArgName("options")
                .hasArgs()
                .withValueSeparator(',')
                .withDescription(  "the specific options of a type project" )
                .isRequired(false)
                .withLongOpt("options")
                .create('o');
		Options options=new Options();
		options.addOption(help).addOption(helpType).addOption(verbose).addOption(type).addOption(source).
			addOption(dest).addOption(option);
		return options;
	}
	
	private static Map<String,String> buildOpts(CommandLine aLine) {
		Map<String, String> oOpts=new HashMap<>();
		if(aLine.hasOption('o')) {
			String[] oStrings = aLine.getOptionValues('o');
			for(String oString:oStrings) {
				String[] oVs=oString.split(":");
				if(oVs.length==2) {
					oOpts.put(oVs[0], oVs[1]);
				}
			}
		}
		return oOpts;
	}
	
    public static void main( String[] args )
    {
    	// build options
    	Options oOpts=buildOptions();
    	// create the parser
        CommandLineParser oParser = new GnuParser();
    	
        try {
            // parse the command line arguments
            CommandLine oLine = oParser.parse( oOpts, args );
         // create the generator
            if(oLine.hasOption('h')) {
            	// Print usage.
            	// automatically generate the help statement
            	HelpFormatter formatter = new HelpFormatter();
            	formatter.printHelp( "raml-codegen", oOpts );
            } else {
            	if(oLine.hasOption("ht")) {
            		Generator.config().printHelp(System.out);
            	} else {
            		if(!oLine.hasOption('s')||!oLine.hasOption('t')) {
            			throw new ParseException("Missing required options: t (type) or s (source)");
            		}
            		Generator oG=Generator.build(oLine.getOptionValue('s'), oLine.hasOption('v'));
            		if(oG!=null) {
            			String oPath="./generated-sources";
            			if(oLine.hasOption('d')) {
            				oPath=oLine.getOptionValue('d');
            			}
            			oG.generate(oLine.getOptionValue('t'), oPath, buildOpts(oLine));
            		} else {
            			Exception oE = new GeneratorException("Error while building the generator from the "+
            					oLine.getOptionValue('s')+"raml source");
            			if(oLine.hasOption('v')) {
            				oE.printStackTrace();
            			}
            			throw oE;
            		}
        		}
            }
        }
        catch( Exception oE ) {
            // oops, something went wrong
            System.err.println( "*ERROR* - Generator has failed.  Reason: " + oE.getMessage() );
            System.err.println( "          You can use -h or --help option to print usage");
            System.err.println( "          You can use -v or --verbose for a full stacktrace");
            
        }
    }
}
