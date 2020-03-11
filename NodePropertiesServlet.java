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

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@SuppressWarnings("serial")
@SlingServlet(paths = { "/bin/quakshop/queryTool/nodeProperties" }, methods = { "GET", "POST" })
public class NodePropertiesServlet extends SlingSafeMethodsServlet {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(NodePropertiesServlet.class);

	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {

		JSONObject jsonObject = null;
		StringBuilder sb = new StringBuilder();

		try {

			String path = request.getParameter("path");

			sb.append("{");

			if (path != null && !path.isEmpty()) {
				LOGGER.info("path : {} ", path);

				Resource resource = request.getResourceResolver().getResource(path);

				if (resource != null) {
					Node node = resource.adaptTo(Node.class);
					PropertyIterator propIt = node.getProperties();
					while (propIt.hasNext()) {

						Property prop = propIt.nextProperty();
						String name = prop.getName();
						String value = "";
						LOGGER.info("prop : {} ", name);

						if (prop.isMultiple()) {

							Value[] values = prop.getValues();
							sb.append("\""+name+"\":");
							sb.append("{");
							int i = 0, maxlength = values.length;
							while (i < maxlength) {

								value = extractPropertyType(values[i]);
								sb.append("\""+i+"\"");
								sb.append(":");
								sb.append("\""+value+"\"");
								i++;
								if (i < maxlength)
									sb.append(",");
							}
							sb.append("}");

						} else {

							value = extractPropertyType(prop);
							sb.append("\""+name+"\"");
							sb.append(":");
							sb.append("\""+value+"\"");
						}

						if (propIt.hasNext())
							sb.append(",");
					}
				}

			} else {
				sb.append("'path':'NULL'");
			}
			
			sb.append("}");

			jsonObject = new JSONObject(sb.toString());
			response.getWriter().write(jsonObject.toString());

		} catch (Exception e) {
			LOGGER.error("GET Exception", e);
			response.getWriter().write("{}");
		}
	}

	private String extractPropertyType(Property property) throws Exception {

		String value;
		if (property.getType() == PropertyType.DATE) {
			value = String.valueOf(property.getDate().getTimeInMillis());
		} else if (property.getType() == PropertyType.BINARY) {
			value = "BINARY: Unable to modify using query tool";
		} else {
			value = property.getString();
		}
		return value;
	}

	private String extractPropertyType(Value propertyValue) throws Exception {

		String value;
		if (propertyValue.getType() == PropertyType.DATE) {
			value = String.valueOf(propertyValue.getDate().getTimeInMillis());
		} else if (propertyValue.getType() == PropertyType.BINARY) {
			value = "BINARY: Unable to modify using query tool";
		} else {
			value = propertyValue.getString();
		}
		return value;
	}

	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {

		try {
			doGet(request, response);

		} catch (Exception e) {
			LOGGER.error("POST Exception");
			LOGGER.error(e.getMessage());
		}
	}
}
