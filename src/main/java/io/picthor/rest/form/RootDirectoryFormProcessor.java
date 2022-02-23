package io.picthor.rest.form;

import com.realcnbs.horizon.framework.FrameworkException;
import com.realcnbs.horizon.framework.rest.form.processor.AbstractFormProcessor;
import com.realcnbs.horizon.framework.rest.form.processor.FormProcessor;
import io.picthor.data.entity.Directory;
import org.springframework.stereotype.Service;

@Service
public class RootDirectoryFormProcessor extends AbstractFormProcessor implements FormProcessor<Directory, RootDirectoryForm> {

    @Override
    public Directory buildEntity(RootDirectoryForm form) {
        Directory directory = new Directory();
        directory.setName(form.getName());
        directory.setFullPath(form.getPath());
        directory.setType(Directory.Type.ROOT);
        directory.setState(Directory.State.ENABLED);
        return directory;
    }

    @Override
    public void updateEntity(Directory toUpdate, RootDirectoryForm form) throws FrameworkException {
        log.info("Updating Root Directory");
        bindNewData(toUpdate, buildEntity(form));
    }

    @Override
    public void processRelations(Directory entity, RootDirectoryForm form) {

    }
}
