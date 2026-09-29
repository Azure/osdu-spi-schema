package org.opengroup.osdu.schema.service;

public interface IEntityTypeService {

    Boolean checkAndRegisterEntityTypeIfNotPresent(String entityTypeId);

    Boolean checkAndRegisterSystemEntityTypeIfNotPresent(String entityTypeId);

}
