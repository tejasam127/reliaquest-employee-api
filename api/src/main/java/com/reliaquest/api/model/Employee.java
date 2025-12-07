package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an Employee entity.
 * Maps to the response from Mock Employee API.
 * Uses @JsonAlias for deserialization from Mock API (employee_* fields)
 * while serializing with clean field names for our API consumers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private UUID id;

    @JsonAlias("employee_name")
    private String name;

    @JsonAlias("employee_salary")
    private Integer salary;

    @JsonAlias("employee_age")
    private Integer age;

    @JsonAlias("employee_title")
    private String title;

    @JsonAlias("employee_email")
    private String email;
}
