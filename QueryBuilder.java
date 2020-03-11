package com.aem4life.quakshop.core.models;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.query.Query;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryBuilder {

	private Map<String, String> queryBuilderMap = new LinkedHashMap<>();
	private ResourceResolver resourceResolver;
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);
	private final String JCR_RESOURCE_TYPE = "sling:resourceType";
	private final String JCR_PRIMARY_TYPE = "jcr:primaryType";
	
	public QueryBuilder(){
		
	}
	
	@SuppressWarnings("unchecked")
	public QueryBuilder(SlingHttpServletRequest  request){
		try {
			resourceResolver = request.getResourceResolver();
			processQuery(request);
		} catch (Exception e) {
			LOGGER.error("Exception while processing query paramenters : ",e);
		}
	}
	
	private void processQuery(SlingHttpServletRequest  request) throws Exception{
		
		Iterator<Resource> resultIt =  null;
		JSONObject tempJsonObject = null;
		String query = request.getParameter("querySearch");
		String operation = request.getParameter("operation");			
		Map<String, Object> tempMap = new LinkedHashMap<>();

		if(operation == null) operation = "read";
		
		if(query == null) {

			query = createFormDataQuery(request);
			resultIt = resourceResolver.findResources(query, Query.JCR_SQL2);

		}else {
			String queryLanguage = request.getParameter("queryLanguage");
			if(queryLanguage != null) {
				resultIt = resourceResolver.findResources(query, queryLanguage);
			}
		}		
		
		while(resultIt.hasNext()){

            Resource r = resultIt.next();
            Node n = r.adaptTo(Node.class);

            if(n != null && r != null){
            	        		
        		tempJsonObject = new JSONObject(createResultEntry(n));
            	tempMap.put(getUniqueId(r), tempJsonObject.toString());
            }
        }

		tempJsonObject = new JSONObject(tempMap);
        queryBuilderMap.put("result", tempJsonObject.toString());
        queryBuilderMap.put("query", query);    		
        queryBuilderMap.put("operation", operation);    		
	}

	private String createFormDataQuery(SlingHttpServletRequest request) throws JSONException {

		JSONObject tempJsonObject;
		JSONArray tempJsonArray;
		Map<String, String> queryParams = new LinkedHashMap<>();
		StringBuilder finalQuery = new StringBuilder();

		String searchIn = request.getParameter("searchIn");
		String fullTextSearch = request.getParameter("fullTextSearch");
		String primaryType = request.getParameter("primaryType");
		String searchProps = request.getParameter("searchProps");
		
		if(searchIn == null) searchIn = "/content";
		if(searchProps != null) {
			tempJsonArray = new JSONArray(searchProps);
			for(int i = 0; i < tempJsonArray.length(); i++) {
				tempJsonObject = tempJsonArray.getJSONObject(i); 
				if(tempJsonObject != null) {
					String name = tempJsonObject.has("name") ? tempJsonObject.getString("name"): "" ;
					String value = tempJsonObject.has("value") ? tempJsonObject.getString("value"): "" ;
					queryParams.put(name, value);
				}
			}
		}
		
		if(primaryType != null && !primaryType.equals("nt:unstructured")) queryParams.put(JCR_PRIMARY_TYPE, primaryType);

		finalQuery.append("SELECT * FROM [nt:unstructured] AS node ");
		finalQuery.append("WHERE ISDESCENDANTNODE(node, '" + searchIn + "')");
		if(fullTextSearch != null) finalQuery.append(String.format(" AND CONTAINS(node.*, '%s')", fullTextSearch));

		if (queryParams != null && !queryParams.isEmpty()) {
			for (String key : queryParams.keySet()) {
				String val = (String) queryParams.get(key);
				finalQuery.append(String.format(" AND node.[%s] = '%s'", key, val));
			}
		}

		String sortOrder = "name";
		if (sortOrder != null && !sortOrder.isEmpty()) {
			finalQuery.append(" order by " + sortOrder);
		}
		return finalQuery.toString();
	}

    private Map<String, Object> createResultEntry(Node n){
       
    	StringBuilder allProps = new StringBuilder();
        Map<String, Object> tempMap = new LinkedHashMap<>();

		try {
	        String resPath = n.getPath();
	        String resName = n.getName();
	        String resType = n.hasProperty(JCR_RESOURCE_TYPE) ? n.getProperty(JCR_RESOURCE_TYPE).getString() 
	        				: n.getProperty(JCR_PRIMARY_TYPE).getString();
	       
	        tempMap.put("path", resPath);
	        tempMap.put("name", resName);
	        tempMap.put("type", resType);

	        PropertyIterator propIt =  n.getProperties();
	        while(propIt.hasNext()) {
	        	Property prop = propIt.nextProperty();
        		allProps.append(prop.getName());
        		allProps.append("<br>");
	        }
	        tempMap.put("properties", allProps.toString());
	        
		} catch (Exception e) {
			LOGGER.error("Exception while creating result entry : ",e);
		} 

        return tempMap;
    }
	
    private String getUniqueId(Resource r) {
        return String.valueOf(Math.abs(r.getPath().hashCode() - 1));
	}
    
	// ************************************************************/
	
	public Map<String, String> getQueryBuilderMap() {
		return queryBuilderMap;
	}

	public void setQueryBuilderMap(Map<String, String> queryBuilderMap) {
		this.queryBuilderMap = queryBuilderMap;
	}
}
