package org.opengroup.osdu.schema.service;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaInfoResponse;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.model.SchemaUpsertResponse;

public interface ISchemaService {

	public Object getSchema(String schemaId) throws BadRequestException, NotFoundException, ApplicationException;

	SchemaInfo createSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException;

	SchemaInfo updateSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException;

	/**
	 * This method first tries to update the schema with the given schema-id. If there is no schema found,
	 * it tries to create the new schema for the given tenant.
	 *
	 * @param schemarequest
	 * @return SchemaUpsertResponse
	 * @throws ApplicationException
	 * @throws BadRequestException
	 */
	SchemaUpsertResponse upsertSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException;

	SchemaInfoResponse getSchemaInfoList(QueryParams queryParams) throws BadRequestException, ApplicationException;

	/**
	 * This method first tries to update the System schema with the given schema-id. If there is no schema found,
	 * it tries to create the new System schema.
	 * @param schemaRequest
	 * @return SchemaUpsertResponse
	 * @throws ApplicationException
	 * @throws BadRequestException
	 */
	SchemaUpsertResponse upsertSystemSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException;

}
