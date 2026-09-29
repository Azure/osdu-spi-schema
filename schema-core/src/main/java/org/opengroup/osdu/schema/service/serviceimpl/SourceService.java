package org.opengroup.osdu.schema.service.serviceimpl;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.model.Source;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.ISourceStore;
import org.opengroup.osdu.schema.service.ISourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Source service to register Source.
 */
@Service
public class SourceService implements ISourceService {

    @Autowired
    ISourceStore sourceStore;

    /**
     * check and create Source if its not present in dataPartitionId store
     *
     * @param sourceId
     * @return true or false of successful registration of Source.
     */
    @Override
    public Boolean checkAndRegisterSourceIfNotPresent(String sourceId) {

        try {
            Source source = new Source();
            source.setSourceId(sourceId);
            this.sourceStore.create(source);
        } catch (ApplicationException e) {
            return false;
        } catch (BadRequestException ex) {
            return true;
        }
        return true;

    }

    /**
     * check and create system Source if its not present
     * @param sourceId
     * @return true or false of successful registration of Source.
     */
    @Override
    public Boolean checkAndRegisterSystemSourceIfNotPresent(String sourceId) {
        try {
            Source source = new Source();
            source.setSourceId(sourceId);
            this.sourceStore.createSystemSource(source);
        } catch (ApplicationException e) {
            return false;
        } catch (BadRequestException ex) {
            return true;
        }
        return true;
    }
}
