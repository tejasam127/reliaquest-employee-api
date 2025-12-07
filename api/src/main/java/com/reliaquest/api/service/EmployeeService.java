package com.reliaquest.api.service;

import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.DeleteEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for interacting with the Mock Employee API.
 * Implements retry logic to handle rate limiting.
 */
@Slf4j
@Service
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final int maxRetries;
    private final long retryDelayMs;

    public EmployeeService(
            RestTemplate restTemplate,
            @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}") String baseUrl,
            @Value("${employee.api.max-retries:3}") int maxRetries,
            @Value("${employee.api.retry-delay-ms:1000}") long retryDelayMs) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    /**
     * Retrieves all employees from the Mock Employee API.
     *
     * @return list of all employees
     */
    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees from Mock Employee API");
        return executeWithRetry(() -> {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.debug(
                        "Successfully retrieved {} employees",
                        response.getBody().getData().size());
                return response.getBody().getData();
            }
            log.warn("Received empty response when fetching all employees");
            return Collections.emptyList();
        });
    }

    /**
     * Searches employees by name fragment.
     *
     * @param searchString the name fragment to search for
     * @return list of employees whose names contain the search string
     */
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees with name containing: {}", searchString);
        List<Employee> allEmployees = getAllEmployees();

        List<Employee> matchingEmployees = allEmployees.stream()
                .filter(emp ->
                        emp.getName() != null && emp.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        log.debug("Found {} employees matching search criteria", matchingEmployees.size());
        return matchingEmployees;
    }

    /**
     * Retrieves an employee by ID.
     *
     * @param id the employee ID
     * @return the employee
     * @throws EmployeeNotFoundException if employee is not found
     */
    public Employee getEmployeeById(String id) {
        log.info("Fetching employee with ID: {}", id);
        return executeWithRetry(() -> {
            try {
                ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                        baseUrl + "/" + id,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<ApiResponse<Employee>>() {});

                if (response.getBody() != null && response.getBody().getData() != null) {
                    log.debug(
                            "Successfully retrieved employee: {}",
                            response.getBody().getData().getName());
                    return response.getBody().getData();
                }
                throw new EmployeeNotFoundException("Employee not found with ID: " + id);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404) {
                    log.warn("Employee not found with ID: {}", id);
                    throw new EmployeeNotFoundException("Employee not found with ID: " + id);
                }
                throw e;
            }
        });
    }

    /**
     * Finds the highest salary among all employees.
     *
     * @return the highest salary
     */
    public Integer getHighestSalary() {
        log.info("Finding highest salary among all employees");
        List<Employee> employees = getAllEmployees();

        return employees.stream()
                .map(Employee::getSalary)
                .filter(salary -> salary != null)
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * Gets the names of top 10 highest earning employees.
     *
     * @return list of employee names sorted by salary (highest first)
     */
    public List<String> getTop10HighestEarningEmployeeNames() {
        log.info("Finding top 10 highest earning employees");
        List<Employee> employees = getAllEmployees();

        List<String> topEarners = employees.stream()
                .filter(emp -> emp.getSalary() != null)
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());

        log.debug("Top 10 highest earners: {}", topEarners);
        return topEarners;
    }

    /**
     * Creates a new employee.
     *
     * @param input the employee creation input
     * @return the created employee
     */
    public Employee createEmployee(CreateEmployeeInput input) {
        log.info("Creating new employee with name: {}", input.getName());
        return executeWithRetry(() -> {
            HttpEntity<CreateEmployeeInput> request = new HttpEntity<>(input);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, request, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info(
                        "Successfully created employee with ID: {}",
                        response.getBody().getData().getId());
                return response.getBody().getData();
            }
            throw new EmployeeApiException("Failed to create employee - empty response received");
        });
    }

    /**
     * Deletes an employee by ID.
     *
     * @param id the employee ID
     * @return the name of the deleted employee
     * @throws EmployeeNotFoundException if employee is not found
     */
    public String deleteEmployeeById(String id) {
        log.info("Deleting employee with ID: {}", id);

        // First, get the employee to retrieve their name
        Employee employee = getEmployeeById(id);
        String employeeName = employee.getName();

        return executeWithRetry(() -> {
            // The Mock API expects a DELETE request with name in the body
            DeleteEmployeeRequest deleteRequest = new DeleteEmployeeRequest(employeeName);
            HttpEntity<DeleteEmployeeRequest> request = new HttpEntity<>(deleteRequest);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.DELETE, request, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            if (response.getBody() != null
                    && response.getBody().getData() != null
                    && response.getBody().getData()) {
                log.info("Successfully deleted employee: {}", employeeName);
                return employeeName;
            }
            throw new EmployeeApiException("Failed to delete employee with ID: " + id);
        });
    }

    /**
     * Executes an operation with retry logic for handling rate limiting.
     *
     * @param operation the operation to execute
     * @param <T> the return type
     * @return the result of the operation
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                return operation.execute();
            } catch (HttpStatusCodeException e) {
                HttpStatusCode statusCode = e.getStatusCode();
                // Retry on rate limiting (429) or server errors (5xx)
                if (statusCode.value() == 429 || statusCode.is5xxServerError()) {
                    attempts++;
                    lastException = e;
                    log.warn(
                            "Rate limited or server error from Mock Employee API (status {}). Attempt {}/{}. Retrying after {}ms...",
                            statusCode.value(),
                            attempts,
                            maxRetries,
                            retryDelayMs);
                    if (attempts < maxRetries) {
                        sleep();
                    }
                } else {
                    // Don't retry client errors (4xx except 429)
                    throw e;
                }
            } catch (RestClientException e) {
                attempts++;
                lastException = e;
                log.warn(
                        "Error communicating with Mock Employee API. Attempt {}/{}. Error: {}",
                        attempts,
                        maxRetries,
                        e.getMessage());
                if (attempts < maxRetries) {
                    sleep();
                }
            }
        }

        log.error("Max retries ({}) exceeded while calling Mock Employee API", maxRetries);
        throw new EmployeeApiException(
                "Failed to communicate with Mock Employee API after " + maxRetries + " attempts", lastException);
    }

    private void sleep() {
        try {
            Thread.sleep(retryDelayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new EmployeeApiException("Retry interrupted", ie);
        }
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute();
    }
}
