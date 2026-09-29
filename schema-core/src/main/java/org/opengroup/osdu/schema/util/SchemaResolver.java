package org.opengroup.osdu.schema.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.service.ISchemaService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Schema resolver class to resolve references inside a schema.
 */

@Component
public class SchemaResolver {

    private ISchemaService schemaService;

    JaxRsDpsLog log;

    public SchemaResolver(@Lazy ISchemaService schemaService, JaxRsDpsLog log) {
        this.schemaService = schemaService;
        this.log = log;
    }

    /**
     * Method to resolve schema references.
     *
     * @param schema
     * @return String
     * @throws BadRequestException
     * @throws ApplicationException
     */
    public String resolveSchema(String schema) throws BadRequestException, ApplicationException {

        JSONObject originalSchema = new JSONObject(schema);

        Map<String, String> refSchemas = new HashMap<>();
        findAndResolveRef(originalSchema, refSchemas);
        JSONObject definition = fetchObjectFromJSON(originalSchema, SchemaConstants.DEFINITIONS);
        Map<String, Object> definitionMap = new HashMap<>();
        if (definition != null) {
            definitionMap = definition.toMap();
        }


        for (Map.Entry<String, String> entry : refSchemas.entrySet()) {

            if (entry.getValue() == null) {
                if (!definitionMap.containsKey(entry.getKey())) {
                    throw new BadRequestException(
                            String.format("Invalid input, no '%s' definition found but provided as a reference", entry.getKey()));
                }
            } else {
                JSONObject refSchema = new JSONObject(entry.getValue());
                JSONObject refSchemaDef = fetchObjectFromJSON(refSchema, SchemaConstants.DEFINITIONS);
                if (refSchemaDef != null)
                    definitionMap.putAll(refSchemaDef.toMap());
                refSchema.remove(SchemaConstants.DEFINITIONS);
                definitionMap.put(entry.getKey(), refSchema);
            }
        }
        if (!definitionMap.isEmpty()) {
            originalSchema.put(SchemaConstants.DEFINITIONS, definitionMap);
        }
        return originalSchema.toString();

    }

    /**
     * A schema can have external/ internal schema references, this methods find and
     * resolves them
     * 
     * @param jsonObject
     * @param refSchemas
     * @return
     * @throws BadRequestException
     * @throws ApplicationException
     */
    private JSONObject findAndResolveRef(JSONObject jsonObject, Map<String, String> refSchemas)
            throws BadRequestException, ApplicationException {

        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            if (key.equalsIgnoreCase(SchemaConstants.REF)) {
                String value = resolveRef(jsonObject.get(key), refSchemas);
                jsonObject.put(key, value);
            } else if (jsonObject.get(key) instanceof JSONObject) {
                findAndResolveRef((JSONObject) jsonObject.get(key), refSchemas);
            } else if (jsonObject.get(key) instanceof JSONArray) {
                findAndResolveRef(jsonObject.getJSONArray(key), refSchemas);
            }
        }
        return jsonObject;
    }

    private JSONArray findAndResolveRef(JSONArray jsonArray, Map<String, String> refSchemas)
            throws BadRequestException, ApplicationException {
        for (Object object : jsonArray) {
            if (object instanceof JSONObject) {
                findAndResolveRef((JSONObject) object, refSchemas);
            } else if (object instanceof JSONArray) {
                findAndResolveRef((JSONArray) object, refSchemas);
            }
        }
        return jsonArray;
    }

    private String resolveRef(Object object, Map<String, String> refSchemas)
            throws BadRequestException, ApplicationException {
        String value = String.valueOf(object);
        if (value.startsWith("#")) {
            refSchemas.put(value.substring(value.lastIndexOf('/') + 1), null);
            return value;
        } else if (value.startsWith("https")) {
            String refSchema = getSchemaFromExternal(value);
            value = value.substring(value.lastIndexOf('/') + 1, value.lastIndexOf('.'));
            refSchemas.put(value, refSchema);
            return String.format("#/definitions/%s", value);

        } else {
            String refSchema = getSchemafromStorage(value);
            refSchemas.put(value, refSchema);
            return String.format("#/definitions/%s", value);
        }
    }

    private String getSchemafromStorage(String value) throws BadRequestException, ApplicationException {

        try {
            return String.valueOf(schemaService.getSchema(value));
        } catch (NotFoundException ex) {
            throw new BadRequestException(
                    String.format("Invalid input, %s not registered but provided as reference", value));
        }

    }

    /**
     * Returning <code>null</code> in case the key is not present. This will help us
     * in building the logic above.
     *
     * @param obj
     * @param value
     * @return {@link JSONObject}
     */
    private JSONObject fetchObjectFromJSON(JSONObject obj, String value) {
        try {
            return (JSONObject) obj.get(value);
        } catch (JSONException e) {
            return null;
        }

    }

    private String getSchemaFromExternal(String url) throws BadRequestException {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.error(String.format("Response code while fetching schema from %s , %d", url, statusCode));
                log.error(String.format("Response received while fetching schema from %s , %s", url,
                        EntityUtils.toString(response.getEntity())));
                throw new BadRequestException(String.format("Invalid Request, %s not resolvable", url));
            }
            String schema = EntityUtils.toString(response.getEntity());

            if (isValidJson(schema))
                return schema;
            else
                throw new BadRequestException(String.format("Invalid Request, %s not a valid Json schema object", url));
        } catch (IOException e) {
            throw new BadRequestException(String.format("Invalid Request, %s not resolvable", url));
        }
    }

    private Boolean isValidJson(String schema) {

        try {
            new JSONObject(schema);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
}
