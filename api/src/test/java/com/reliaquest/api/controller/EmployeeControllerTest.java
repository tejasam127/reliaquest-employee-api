package com.reliaquest.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private Employee createEmployee(String id, String name, int salary, int age) {
        return Employee.builder()
                .id(UUID.fromString(id))
                .name(name)
                .salary(salary)
                .age(age)
                .title("Engineer")
                .email(name.toLowerCase().replace(" ", "") + "@company.com")
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/employee")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("should return all employees")
        void shouldReturnAllEmployees() throws Exception {
            List<Employee> employees = Arrays.asList(
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "John Doe", 50000, 30),
                    createEmployee("5255f1a5-f9f7-4be5-829a-134bde088d17", "Jane Smith", 60000, 35));

            when(employeeService.getAllEmployees()).thenReturn(employees);

            mockMvc.perform(get("/api/v1/employee"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("John Doe"))
                    .andExpect(jsonPath("$[1].name").value("Jane Smith"));
        }

        @Test
        @DisplayName("should return empty list when no employees")
        void shouldReturnEmptyList() throws Exception {
            when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/employee"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employee/search/{searchString}")
    class SearchEmployeesTests {

        @Test
        @DisplayName("should return employees matching search string")
        void shouldReturnMatchingEmployees() throws Exception {
            List<Employee> employees =
                    Arrays.asList(createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "John Doe", 50000, 30));

            when(employeeService.getEmployeesByNameSearch("John")).thenReturn(employees);

            mockMvc.perform(get("/api/v1/employee/search/John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("John Doe"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employee/{id}")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("should return employee when found")
        void shouldReturnEmployee() throws Exception {
            String id = "4a3a170b-22cd-4ac2-aad1-9bb5b34a1507";
            Employee employee = createEmployee(id, "John Doe", 50000, 30);

            when(employeeService.getEmployeeById(id)).thenReturn(employee);

            mockMvc.perform(get("/api/v1/employee/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.salary").value(50000));
        }

        @Test
        @DisplayName("should return 404 when employee not found")
        void shouldReturn404WhenNotFound() throws Exception {
            String id = "non-existent-id";

            when(employeeService.getEmployeeById(id))
                    .thenThrow(new EmployeeNotFoundException("Employee not found with ID: " + id));

            mockMvc.perform(get("/api/v1/employee/" + id)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employee/highestSalary")
    class GetHighestSalaryTests {

        @Test
        @DisplayName("should return highest salary")
        void shouldReturnHighestSalary() throws Exception {
            when(employeeService.getHighestSalary()).thenReturn(150000);

            mockMvc.perform(get("/api/v1/employee/highestSalary"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("150000"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employee/topTenHighestEarningEmployeeNames")
    class GetTopTenEarnersTests {

        @Test
        @DisplayName("should return top 10 earner names")
        void shouldReturnTopTenEarners() throws Exception {
            List<String> names =
                    Arrays.asList("CEO", "CTO", "CFO", "VP1", "VP2", "Dir1", "Dir2", "Mgr1", "Mgr2", "Mgr3");

            when(employeeService.getTop10HighestEarningEmployeeNames()).thenReturn(names);

            mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(10))
                    .andExpect(jsonPath("$[0]").value("CEO"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/employee")
    class CreateEmployeeTests {

        @Test
        @DisplayName("should create employee successfully")
        void shouldCreateEmployee() throws Exception {
            CreateEmployeeInput input = CreateEmployeeInput.builder()
                    .name("New Employee")
                    .salary(55000)
                    .age(28)
                    .title("Software Engineer")
                    .build();

            Employee createdEmployee =
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "New Employee", 55000, 28);

            when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(createdEmployee);

            mockMvc.perform(post("/api/v1/employee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("New Employee"))
                    .andExpect(jsonPath("$.salary").value(55000));
        }

        @Test
        @DisplayName("should return 400 for invalid input")
        void shouldReturn400ForInvalidInput() throws Exception {
            CreateEmployeeInput input = CreateEmployeeInput.builder()
                    .name("") // Invalid: blank name
                    .salary(-100) // Invalid: negative salary
                    .age(10) // Invalid: too young
                    .title("")
                    .build();

            mockMvc.perform(post("/api/v1/employee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for missing required fields")
        void shouldReturn400ForMissingFields() throws Exception {
            String invalidJson = "{}";

            mockMvc.perform(post("/api/v1/employee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/employee/{id}")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("should delete employee and return name")
        void shouldDeleteEmployee() throws Exception {
            String id = "4a3a170b-22cd-4ac2-aad1-9bb5b34a1507";

            when(employeeService.deleteEmployeeById(id)).thenReturn("John Doe");

            mockMvc.perform(delete("/api/v1/employee/" + id))
                    .andExpect(status().isOk())
                    .andExpect(content().string("John Doe"));
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent employee")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            String id = "non-existent-id";

            when(employeeService.deleteEmployeeById(id))
                    .thenThrow(new EmployeeNotFoundException("Employee not found with ID: " + id));

            mockMvc.perform(delete("/api/v1/employee/" + id)).andExpect(status().isNotFound());
        }
    }
}
