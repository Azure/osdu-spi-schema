
package org.opengroup.osdu.schema.provider.interfaces.schemainfostore;

import java.util.List;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.apache.commons.lang3.NotImplementedException;

public interface ISchemaInfoStore {

    SchemaInfo updateSchemaInfo(SchemaRequest schema) throws ApplicationException, BadRequestException;

    default SchemaInfo updateSystemSchemaInfo(SchemaRequest schema) throws ApplicationException, BadRequestException {
        throw new NotImplementedException();
    }

    SchemaInfo createSchemaInfo(SchemaRequest schema) throws ApplicationException, BadRequestException;

    default SchemaInfo createSystemSchemaInfo(SchemaRequest schema) throws ApplicationException, BadRequestException {
        throw new NotImplementedException();
    }

    SchemaInfo getSchemaInfo(String schemaId) throws ApplicationException, NotFoundException;

    default SchemaInfo getSystemSchemaInfo(String schemaId) throws ApplicationException, NotFoundException {
        throw new NotImplementedException();
    }

    String getLatestMinorVerSchema(SchemaInfo schemaInfo) throws ApplicationException;

    List<SchemaInfo> getSchemaInfoList(QueryParams queryParams, String tenantId) throws ApplicationException;

    default List<SchemaInfo> getSystemSchemaInfoList(QueryParams queryParams) throws ApplicationException {
        throw new NotImplementedException();
    }

    boolean isUnique(String schemaId, String tenantId) throws ApplicationException;

    default boolean isUniqueSystemSchema(String schemaId) throws ApplicationException {
        throw new NotImplementedException();
    }

    boolean cleanSchema(String schemaId) throws ApplicationException;

    default boolean cleanSystemSchema(String schemaId) throws ApplicationException {
        throw new NotImplementedException();
    }
}
