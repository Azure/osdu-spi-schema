package org.opengroup.osdu.schema.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.model.schema.SchemaInfo;
import org.opengroup.osdu.core.test.client.model.schema.SchemaModel;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;

/**
 * Helper that catches {@link ClientException} thrown by {@link org.opengroup.osdu.core.test.client.SchemaClient}
 * on non-2xx responses and stores the status code and exception in the scenario context.
 */
public class SchemaClientExceptionSupport {

    /**
     * Runs the given action. If it throws a {@link ClientException}, stores the status code
     * and exception in the context instead of propagating.
     */
    public static void tryRun(SchemaServiceScope context, Runnable action) {
        try {
            action.run();
        } catch (ClientException e) {
            context.setLastStatusCode(e.getStatusCode());
            context.setLastClientException(e);
        }
    }

    /**
     * Constructs a {@link SchemaModel} from a {@link JsonElement} without round-tripping the
     * {@code schema} body through Gson object deserialisation.  This avoids the int→double
     * corruption that occurs when Gson deserialises JSON numbers into {@code Object} fields.
     */
    public static SchemaModel buildModel(JsonElement json) {
        SchemaModel m = new SchemaModel();
        m.setSchemaInfo(new Gson().fromJson(json.getAsJsonObject().get("schemaInfo"), SchemaInfo.class));
        m.setSchema(json.getAsJsonObject().get("schema")); // stays as JsonElement → no int→double
        return m;
    }
}
