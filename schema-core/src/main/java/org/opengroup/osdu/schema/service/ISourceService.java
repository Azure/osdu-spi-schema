package org.opengroup.osdu.schema.service;

public interface ISourceService {

    Boolean checkAndRegisterSourceIfNotPresent(String sourceId);

    Boolean checkAndRegisterSystemSourceIfNotPresent(String sourceId);

}
