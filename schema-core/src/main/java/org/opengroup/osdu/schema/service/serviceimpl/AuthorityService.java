package org.opengroup.osdu.schema.service.serviceimpl;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.model.Authority;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.IAuthorityStore;
import org.opengroup.osdu.schema.service.IAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Authority service to register authority.
 *
 */
@Service
public class AuthorityService implements IAuthorityService {

    @Autowired
    IAuthorityStore authorityStore;

    /**
     * check and create authority if its not present in the tenant dataPartitionId
     *
     * @param authorityId
     * @return true or false of successful registration of authority.
     */
    @Override
    public Boolean checkAndRegisterAuthorityIfNotPresent(String authorityId) {
        try {
            Authority authority = new Authority();
            authority.setAuthorityId(authorityId);
            authorityStore.create(authority);
        } catch (ApplicationException e) {
            return false;
        } catch (BadRequestException ex) {
            return true;
        }
        return true;
    }

    /**
     * check and create System authority if its not present
     * @param authorityId
     * @return
     */
    @Override
    public Boolean checkAndRegisterSystemAuthorityIfNotPresent(String authorityId) {
        try {
            Authority authority = new Authority();
            authority.setAuthorityId(authorityId);
            authorityStore.createSystemAuthority(authority);
        } catch (ApplicationException e) {
            return false;
        } catch (BadRequestException ex) {
            return true;
        }
        return true;
    }

}
