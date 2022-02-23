package io.picthor.rest.validation.constraint;


import io.picthor.rest.validation.validator.RootPathValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = RootPathValidator.class)
public @interface ValidRootPath {

    String message() default "root path is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
