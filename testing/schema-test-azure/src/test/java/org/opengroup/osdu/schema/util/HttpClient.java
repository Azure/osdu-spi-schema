package org.opengroup.osdu.schema.util;

import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;

public interface HttpClient {
	HttpResponse send(HttpRequest httpRequest);

	<T> T send(HttpRequest httpRequest, Class<T> classOfT);
}
