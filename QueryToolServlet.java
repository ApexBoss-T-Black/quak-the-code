/*
 *  Copyright 2014 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem4life.quakshop.core.impl.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem4life.quakshop.core.models.QueryBuilder;
/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@SuppressWarnings("serial")
@SlingServlet(paths = { "/bin/quakshop/queryTool" }, methods = { "GET", "POST" })
public class QueryToolServlet extends SlingSafeMethodsServlet {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryToolServlet.class);

	/** The map of query results */
	private Map<String, String> queryBuilderMap = new HashMap<>();

	@Override
	protected void doGet(final SlingHttpServletRequest request,
			final SlingHttpServletResponse response) throws ServletException, IOException {
		
		try {
						
			QueryBuilder qb = new QueryBuilder(request);
			queryBuilderMap = qb.getQueryBuilderMap();		
			
			JSONObject jsonObject = new JSONObject(queryBuilderMap);
			response.getWriter().write(jsonObject.toString());
			
		} catch (Exception e) {
			LOGGER.error("GET Exception");
			LOGGER.error(e.getMessage());
		}		

	}

	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		LOGGER.info("INSIDE QUERY SERVLET... POST");
		try {

			doGet(request, response);

		} catch (Exception e) {
			LOGGER.error("POST Exception");
			LOGGER.error(e.getMessage());
			LOGGER.error("INSIDE QUERY SERVLET... error", e);

		}
	}
	/*
	private void advancedQueryBuilderTool() {
		
		Session session = null;
		QueryBuilder builder = null;
		
		//Invoke the adaptTo method to create a Session used to create a QueryManager
        session = resourceResolver.adaptTo(Session.class);
                   
        String fulltextSearchTerm = "Geometrixx";
                        
        // create query description as hash map (simplest way, same as form post)
        Map<String, String> map = new HashMap<String, String>();
       
     // create query description as hash map (simplest way, same as form post)
                     
        map.put("path", "/content");
        map.put("type", "cq:Page");
        map.put("group.p.or", "true"); // combine this group with OR
        map.put("group.1_fulltext", fulltextSearchTerm);
        map.put("group.1_fulltext.relPath", "jcr:content");
        map.put("group.2_fulltext", fulltextSearchTerm);
        map.put("group.2_fulltext.relPath", "jcr:content/@cq:tags");
        // can be done in map or with Query methods
        map.put("p.offset", "0"); // same as query.setStart(0) below
        map.put("p.limit", "20"); // same as query.setHitsPerPage(20) below
                          
        Query query = builder.createQuery(PredicateGroup.create(map), session);
                            
        query.setStart(0);
        query.setHitsPerPage(20);
                    
        SearchResult result = query.getResult();
                        
        // paging metadata
        int hitsPerPage = result.getHits().size(); // 20 (set above) or lower
        long totalMatches = result.getTotalMatches();
        long offset = result.getStartIndex();
        long numberOfPages = totalMatches / 20;
                       
        //Place the results in XML to return to client
        DocumentBuilderFactory factory =         DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.newDocument();
	                                    
        //Start building the XML to pass back to the AEM client
        Element root = doc.createElement( "results" );
        doc.appendChild( root );
                       
                       
        // iterating over the results
        for (Hit hit : result.getHits()) {
               String path = hit.getPath();
                //Create a result element
                Element resultel = doc.createElement( "result" );
                root.appendChild( resultel );
                           
                Element pathel = doc.createElement( "path" );
                pathel.appendChild( doc.createTextNode(path ) );
                resultel.appendChild( pathel );
                                         
        }
       
        //close the session
        session.logout();  
       
        //Return the JSON formatted data
   // response.getWriter().write(convertToString(doc));
    
		
	}
	 private String convertToString(Document xml)
     {
     try {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
       StreamResult result = new StreamResult(new StringWriter());
       DOMSource source = new DOMSource(xml);
       transformer.transform(source, result);
       return result.getWriter().toString();
     } catch(Exception ex) {
               ex.printStackTrace();
     }
       return null;
          } 
          
          
      }*/

}
