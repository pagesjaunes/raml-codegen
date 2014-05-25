package fr.pagesjaunes.tools.ramlcodegen.renderers.adaptators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import fr.pagesjaunes.tools.ramlcodegen.Utils;

public class ActionAdaptator extends ObjectModelAdaptor {
	
//	private static final Logger logger = LoggerFactory.getLogger(ActionAdaptator.class);
	
	private Raml root;

	public ActionAdaptator(Raml aRoot) {
		root=aRoot;
	}
	
	public static class KeyValue<T> {
		private String key;
		private T value;
		
		private KeyValue(String aKey, T aValue) {
			key=aKey;
			value=aValue;
		}
		
		public String getKey() {
			return key;
		}
		
		public T getValue() {
			return value;
		}
	}
	
	public static class UrlWrapper {
		private String name;
		private String url;
		
		private UrlWrapper(Object aName, String anUrl) {
			name=aName.toString();
			url=anUrl;
		}
		
		@Override
		public String toString() {
			return url;
		}
		
		public String getName() {
			return name;
		}
		
		private static List<UrlWrapper> create(List<String> urls) {
			int oName=0;
			List<UrlWrapper> oReturn=new ArrayList<>();
			for(String oUrl:urls) {
				oReturn.add(new UrlWrapper(oName, oUrl));
				oName++;
			}
			return oReturn;
		}
	}
	
	public class ResponseWrapper {
		
		private Response response;
		private Action action;
		private String status;
		
		public Response getResponse() {
			return response;
		}
		
		public Action getAction() {
			return action;
		}
		
		private ResponseWrapper(Action anAction, Response aResponse, String aStatus) {
			response=aResponse;
			action=anAction;
			status=aStatus;
		}
		
		public String getStatus() {
			return status;
		}
		
		public String getDefaultMediaType() {
			if (root.getMediaType()!=null) {
				return root.getMediaType();
			}
			// If not default content-type, we try to find one.
			if(response.hasBody()) {
				for(String oC:response.getBody().keySet()) {
					if(!oC.equals("*/*")) {
						return oC;
					}
				}
			}
			return null;
		}
		
		public MimeType getDefaultBody() {
			if(getDefaultMediaType()==null) {
				return null;
			}
			if(response.getBody().containsKey(getDefaultMediaType())) {
				return response.getBody().get(getDefaultMediaType());
			}
			if(response.getBody().containsKey("*/*")) {
				return response.getBody().get("*/*");
			}
			return null;
		}
		
		public List<String> getBodyKeys() {
			if(response.getBody()==null) {
				return null;
			}
			ArrayList<String> oReturn=new ArrayList<>();
			for(String k:response.getBody().keySet()) {
				if(!k.equals("*/*")&&!k.equals(getDefaultMediaType())) {
					oReturn.add(k);
				}
			}
			return oReturn;
		}
		
		public MimeType getStarBody() {
			if(response.getBody()==null) {
				return null;
			}
			return response.getBody().get("*/*");
		}
	    		
		private String resolve(String aUrl) {
			Resource oResource=action.getResource();
			while(oResource!=null) {
				aUrl=Resolver.resolve(aUrl, Resolver.fromBaseToUri(oResource.getBaseUriParameters()), "[BaseUriParameter]");
				oResource=oResource.getParentResource();
			}
			if(root.getVersion()!=null) {
				aUrl=Utils.resolve(aUrl, "version", root.getVersion());
			}
			return aUrl;
		}
		
		private List<String> generateCombinationsOpt(String aUrl, Map<String, QueryParameter> aParams, String aSep) {
			List<String> oReturn=new ArrayList<String>(Arrays.asList(aUrl));
			Map<String, QueryParameter> oParams=new HashMap<String, QueryParameter>(aParams);
			for(Map.Entry<String, QueryParameter> oE:aParams.entrySet()) {
				String oUrl=aUrl;
				String oSep=aSep;
				if(!oE.getValue().isRequired()) {
					oUrl+=oSep+oE.getKey()+"="+Utils.encode(Resolver.getValueParam(oE.getValue(), oE.getKey(), "[QueryParameter]"));
					oSep="&";
					oParams.remove(oE.getKey());
					oReturn.addAll(generateCombinationsOpt(oUrl, oParams, oSep));
				}
			}
			return oReturn;
		}
		
		private List<String> generateCombinations(String aUrl) {
			Map<String, QueryParameter> oParameters=action.getQueryParameters();
			String oSep="?";
			for(Map.Entry<String, QueryParameter> oE:oParameters.entrySet()) {
				if(oE.getValue().isRequired()) {
					aUrl+=oSep+oE.getKey()+"="+Utils.encode(Resolver.getValueParam(oE.getValue(), oE.getKey(), "[QueryParameter]"));
					oSep="&";
				}
			}
			return generateCombinationsOpt(aUrl, oParameters, oSep);
		}
		
		private String buildTemplate() {
			Resource oResource=action.getResource();
			String oUrl=oResource.getRelativeUri();
			while(oResource.getParentResource()!=null) {
				oResource=oResource.getParentResource();
				oUrl=oResource.getRelativeUri()+oUrl;
			}
			if(!Utils.hasBeenResolved(root.getBaseUri())) {
				oUrl=root.getBaseUri()+oUrl;
			}
			oResource=action.getResource();
			while(oResource!=null) {
				oUrl=Resolver.resolve(oUrl, oResource.getUriParameters(), "[UriParameter]");
				oResource=oResource.getParentResource();
			}
			return oUrl;
		}
		
		private boolean hasRequired() {
			for(Map.Entry<String, QueryParameter> oE:action.getQueryParameters().entrySet()) {
				if(oE.getValue().isRequired()) {
					return true;
				}
			}
			return false;
		}
		
		public List<UrlWrapper> getCombinations() {	
		    return UrlWrapper.create(generateCombinations(resolve(buildTemplate())));
		}
		
		public List<UrlWrapper> getFalseCombinations() {
			if(hasRequired()) {
				return UrlWrapper.create(generateCombinationsOpt(resolve(buildTemplate()),action.getQueryParameters(),"?"));
			}
			return null;
		}
		
		public Map<String, String> getMandatoryHeaders() {
			Map<String, Header> oHeaders=getAction().getHeaders();
			Map<String, String> oReturn=new HashMap<String, String>();
			for(Map.Entry<String, Header> oHeader:oHeaders.entrySet()) {
				if(oHeader.getValue().isRequired()) {
					String oValue=Resolver.getValueParam(oHeader.getValue(), oHeader.getKey(),"[Mandatory Header]");
					oReturn.put(oHeader.getKey(),oValue);
				}
			}
			return oReturn;
		}
		
		public boolean isMultipleMandatoryHeaders() {
			return getMandatoryHeaders().keySet().size()>1;
		}
		
		public String toString() {
			return response!=null?"Response OK":"Response KO";
		}
	}

	@Override
	public synchronized Object getProperty(Interpreter anInter, ST aSt,
			Object anObject, Object aProperty, String aName)
			throws STNoSuchPropertyException {
		Action oAction=(Action)anObject;
		if(aName.equals("ok")) {
			for(String oStatus:Arrays.asList("200","201","206","202", "204")) {
				Response oRes=oAction.getResponses().get(oStatus);
				if(oRes!=null) {
					return new ResponseWrapper(oAction, oRes, oStatus);
				}
			}
			return null;
		}
		if(aName.equals("okorko")) {
			for(String oStatus:Arrays.asList("200","201","206","202", "204","400","401","403","404", "405", "406", "415","501","500")) {
				Response oRes=oAction.getResponses().get(oStatus);
				if(oRes!=null) {
					return new ResponseWrapper(oAction, oRes, oStatus);
				}
			}
			return null;
		}
		if(aName.equals("nok")) {
			List<ResponseWrapper> oReturn = new ArrayList<>();
			for(String oStatus:Arrays.asList("400","401","403","404", "405", "406", "415")) {
				Response oRes=oAction.getResponses().get(oStatus);
				if(oRes!=null) {
					oReturn.add(new ResponseWrapper(oAction, oRes, oStatus));
				}
			}
			return oReturn;
		}
		if(aName.equals("normalMethod")) {
			if(oAction.getBody()==null||oAction.getBody().isEmpty()) {
				return true;
			}
			for(String oType:oAction.getBody().keySet()) {
				if(!oType.equalsIgnoreCase("application/x-www-form-urlencoded")&&!oType.equalsIgnoreCase("multipart/form-data")) {
					return true;
				}
			}
			return false;
		}
		if(aName.equals("hasFormParams")) {
			if(oAction.getBody()==null||oAction.getBody().isEmpty()) {
				return false;
			}
			return (oAction.getBody().get("application/x-www-form-urlencoded")!=null&&!oAction.getBody().get("application/x-www-form-urlencoded").getFormParameters().isEmpty())||
					(oAction.getBody().get("multipart/form-data")!=null&&!oAction.getBody().get("multipart/form-data").getFormParameters().isEmpty());
		}
		if(aName.equals("formParams")) {
			 Map<String,List<FormParameter>> oParams=oAction.getBody().get("application/x-www-form-urlencoded").getFormParameters();
			 if(oParams.isEmpty()) {
				 oParams=oAction.getBody().get("multipart/form-data").getFormParameters(); 
			 }
			 List<KeyValue<String>> oReturn = new ArrayList<>();
			 // TODO : we generates tests from only one type. Multi type values are not yet supported.
			 for(Map.Entry<String, List<FormParameter>> oE:oParams.entrySet()) {
				 oReturn.add(new KeyValue<String>(oE.getKey(), Resolver.getValueParam(oE.getValue().get(0), oE.getKey(), "[Form Paramater]")));
			 }
			 return oReturn;
		}
		if(aName.equals("mandatoryQueryParams")) {
			List<String> oReturn=new ArrayList<>();
			for(Map.Entry<String, QueryParameter> oE:oAction.getQueryParameters().entrySet()) {
				if(oE.getValue().isRequired()) {
					oReturn.add(oE.getKey());
				}
			}
			return oReturn;
		}
		return super.getProperty(anInter, aSt, anObject, aProperty, aName);
	}
}
