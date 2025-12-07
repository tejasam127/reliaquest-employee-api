package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Employee operations.
 * Implements the IEmployeeController interface as required by the assessment.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeInput> {

    private final EmployeeService employeeService;

    /**
     * Returns all employees.
     *
     * @return list of all employees
     */
    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("GET /api/v1/employee - Fetching all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        log.info("Returning {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    /**
     * Searches employees by name fragment.
     *
     * @param searchString the name fragment to search for
     * @return list of employees whose names contain the search string
     */
    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("GET /api/v1/employee/search/{} - Searching employees by name", searchString);
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString);
        log.info("Found {} employees matching '{}'", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    /**
     * Retrieves an employee by ID.
     *
     * @param id the employee ID
     * @return the employee
     */
    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("GET /api/v1/employee/{} - Fetching employee by ID", id);
        Employee employee = employeeService.getEmployeeById(id);
        log.info("Found employee: {}", employee.getName());
        return ResponseEntity.ok(employee);
    }

    /**
     * Returns the highest salary among all employees.
     *
     * @return integer value of the highest salary
     */
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("GET /api/v1/employee/highestSalary - Fetching highest salary");
        Integer highestSalary = employeeService.getHighestSalary();
        log.info("Highest salary: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    /**
     * Returns the names of the top 10 highest earning employees.
     *
     * @return list of employee names
     */
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("GET /api/v1/employee/topTenHighestEarningEmployeeNames - Fetching top earners");
        List<String> topEarners = employeeService.getTop10HighestEarningEmployeeNames();
        log.info("Returning top {} earners", topEarners.size());
        return ResponseEntity.ok(topEarners);
    }

    /**
     * Creates a new employee.
     *
     * @param employeeInput the employee creation input
     * @return the created employee
     */
    @Override
    public ResponseEntity<Employee> createEmployee(@Valid CreateEmployeeInput employeeInput) {
        log.info("POST /api/v1/employee - Creating employee: {}", employeeInput.getName());
        Employee createdEmployee = employeeService.createEmployee(employeeInput);
        log.info("Created employee with ID: {}", createdEmployee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    /**
     * Deletes an employee by ID.
     *
     * @param id the employee ID
     * @return the name of the deleted employee
     */
    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("DELETE /api/v1/employee/{} - Deleting employee", id);
        String deletedEmployeeName = employeeService.deleteEmployeeById(id);
        log.info("Deleted employee: {}", deletedEmployeeName);
        return ResponseEntity.ok(deletedEmployeeName);
    }
}
