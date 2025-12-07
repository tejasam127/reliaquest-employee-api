package com.reliaquest.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for deleting an employee from Mock Employee API.
 * The Mock API expects a name field in the request body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteEmployeeRequest {

    private String name;
}
