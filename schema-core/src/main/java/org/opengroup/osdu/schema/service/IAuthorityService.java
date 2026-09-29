package org.opengroup.osdu.schema.service;

public interface IAuthorityService {

    Boolean checkAndRegisterAuthorityIfNotPresent(String authorityId);

    Boolean checkAndRegisterSystemAuthorityIfNotPresent(String authorityId);

}
