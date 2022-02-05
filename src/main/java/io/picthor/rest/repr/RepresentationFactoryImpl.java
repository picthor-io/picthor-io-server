package io.picthor.rest.repr;

import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import com.realcnbs.horizon.framework.rest.exception.NoSuchReprException;
import com.realcnbs.horizon.framework.rest.repr.PagedEntityRepr;
import com.realcnbs.horizon.framework.rest.repr.Representation;
import com.realcnbs.horizon.framework.rest.repr.RepresentationFactory;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.BatchJobItem;
import io.picthor.data.entity.FileData;
import io.picthor.data.entity.Directory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RepresentationFactoryImpl implements RepresentationFactory {

    @Override
    public PagedEntityRepr buildPaged(Page page) throws NoSuchReprException {
        PagedEntityRepr pagedRepr = new PagedEntityRepr(page);
        List<Representation> representations = new ArrayList<>();

        // build all the representations
        for (Object entity : page) {
            representations.add(build((AbstractEntity) entity));
        }
        pagedRepr.setRepresentations(representations);
        return pagedRepr;
    }

    @Override
    public Representation build(AbstractEntity entity) throws NoSuchReprException {
        if (entity instanceof BatchJob) {
            return new BatchJobRepr((BatchJob) entity);
        }
        if (entity instanceof BatchJobItem) {
            return new BatchJobItemRepr((BatchJobItem) entity);
        }
        if (entity instanceof FileData) {
            return new FileDataRepr((FileData) entity);
        }
        if (entity instanceof Directory) {
            return new DirectoryRepr((Directory) entity);
        }
        throw new NoSuchReprException("Cannot load representation for entity of type: " + entity.getClass().getName());
    }
}
