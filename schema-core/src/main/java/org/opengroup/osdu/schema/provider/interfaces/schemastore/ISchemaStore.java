package org.opengroup.osdu.schema.provider.interfaces.schemastore;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.apache.commons.lang3.NotImplementedException;

public interface ISchemaStore {

    String createSchema(String filePath, String content) throws ApplicationException;

    default String createSystemSchema(String filePath, String content) throws ApplicationException {
        throw new NotImplementedException();
    }

    String getSchema(String dataPartitionId, String filePath) throws NotFoundException, ApplicationException;

    default String getSystemSchema(String filePath) throws NotFoundException, ApplicationException {
        throw new NotImplementedException();
    }

    boolean cleanSchemaProject(String schemaId) throws ApplicationException;

    default boolean cleanSystemSchemaProject(String schemaId) throws ApplicationException {
        throw new NotImplementedException();
    }
}
