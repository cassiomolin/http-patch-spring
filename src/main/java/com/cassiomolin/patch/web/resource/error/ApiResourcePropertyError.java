package com.cassiomolin.patch.web.resource.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResourcePropertyError {

    private String property;

    private String message;

    @JsonInclude
    private Object invalidValue;
}
