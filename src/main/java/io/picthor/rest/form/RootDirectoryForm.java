package io.picthor.rest.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realcnbs.horizon.framework.rest.form.definition.AbstractForm;
import io.picthor.rest.validation.constraint.ValidRootPath;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
public class RootDirectoryForm extends AbstractForm {

    @Size(max = 100, min = 2)
    @NotEmpty
    @JsonProperty
    private String name;

    @Size(max = 1000, min = 1)
    @NotEmpty
    @JsonProperty
    @ValidRootPath
    private String path;


}