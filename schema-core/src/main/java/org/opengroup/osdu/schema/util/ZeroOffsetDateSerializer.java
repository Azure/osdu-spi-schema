package org.opengroup.osdu.schema.util;

import java.util.Date;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.util.StdDateFormat;

/**
 * Writes a {@link Date} the way the Jackson 2 mapper did ("2019-05-23T11:16:03.123+00:00"). Jackson 3 writes a zero
 * offset as "Z" instead, which would change the schema API response for every service running in UTC.
 */
public class ZeroOffsetDateSerializer extends StdSerializer<Date> {

	public ZeroOffsetDateSerializer() {
		super(Date.class);
	}

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializationContext ctxt) {
		gen.writeString(new StdDateFormat().withTimeZone(ctxt.getTimeZone()).withLocale(ctxt.getLocale())
				.withZeroOffsetAsZ(false).format(value));
	}
}
