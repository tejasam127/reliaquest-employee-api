package com.reliaquest.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input DTO for creating a new Employee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeInput {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Salary is required") @Positive(message = "Salary must be positive") private Integer salary;

    @NotNull(message = "Age is required") @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer age;

    @NotBlank(message = "Title is required")
    private String title;
}
