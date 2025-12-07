package com.reliaquest.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for Mock Employee API responses.
 *
 * @param <T> the type of data contained in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private T data;
    private String status;
    private String error;
}
