package fr.pagesjaunes.tools.ramlcodegen;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

//import org.apache.commons.lang3.StringUtils;

//import com.jayway.restassured.http.ContentType;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.*;

/**
 * Unit test for simple App.
 */
public class GeneratorTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GeneratorTest( String testName )
    {
    	super( testName );
//    	Response oResponse =
//    	         given().
//    	                headers("toto","titi").
//    	                contentType("*/*").
//    	         when().
//    	                get("http://book.e-bookmobile.com/v1/books/Madame+Bovary").
//    	         then().
//    	         extract().
//    	                response();
//    	Iterator<Header> oHeaders=oResponse.getHeaders().iterator();
//    	while(oHeaders.hasNext()) {
//    		if(Pattern.matches("rttt", oHeaders.next().getName())) {
//    			return;
//    		}
//    	}
//    	assertThat(oResponse.getBody().asString(), matchesXsd(""));
//    	assertNotNull(oResponse.getHeaders().get("aName"));
//    	Pattern.matches("totot", "titi");
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GeneratorTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	Generator oG=Generator.build("test.raml");
        assertNotNull(oG);
        oG.generate("mocks","target/generated-sources");
    }
    
    /**
     * Rigourous Test :-)
     */
    public void testGeo()
    {
//    	Generator oG=Generator.build("api_geo.raml");
//        assertNotNull(oG);
//        oG.generate("mocks","target/generated-sources");
    }
    
    /**
     * Rigourous Test :-)
     */
    public void testStripe()
    {
//    	Generator oG=Generator.build("stripe.raml");
//        assertNotNull(oG);
//        oG.generate("mocks","target/generated-sources");
    }
    
    /**
     * Rigourous Test :-)
     */
    public void testTwitter()
    {
//    	Generator oG=Generator.build("twitter.raml");
//        assertNotNull(oG);
//        oG.generate("mocks","target/generated-sources");
    }
}
