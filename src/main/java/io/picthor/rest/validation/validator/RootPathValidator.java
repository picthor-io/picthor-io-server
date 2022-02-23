package io.picthor.rest.validation.validator;

import com.realcnbs.horizon.framework.rest.validation.validator.AbstractValidator;
import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.entity.Directory;
import io.picthor.rest.validation.constraint.ValidRootPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;

@Service
@Slf4j
public class RootPathValidator extends AbstractValidator implements ConstraintValidator<ValidRootPath, String> {

    private final DirectoryDao directoryDao;

    public RootPathValidator(DirectoryDao directoryDao) {
        this.directoryDao = directoryDao;
    }

    @Override
    public void initialize(ValidRootPath constraintAnnotation) {

    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        log.debug("Validating roo path: {}", path);
        Directory existing = directoryDao.findByFullPath(path);
        if (existing != null) {
            log.debug("Root path: {} exists", path);
            setMessage("Root directory with path " + path + " already exists", context);
            return false;
        }
        if (!Path.of(path).toFile().exists()) {
            setMessage("path " + path + " does not exist on system", context);
            return false;
        }
        if (!Path.of(path).toFile().canRead()) {
            setMessage("path " + path + " is not readable", context);
            return false;
        }
        return true;
    }
}