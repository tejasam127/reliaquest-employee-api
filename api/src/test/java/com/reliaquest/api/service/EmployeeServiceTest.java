package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private EmployeeService employeeService;

    private static final String BASE_URL = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(restTemplate, BASE_URL, 3, 100);
    }

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
    @DisplayName("getAllEmployees")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("should return all employees successfully")
        void shouldReturnAllEmployees() {
            List<Employee> employees = Arrays.asList(
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "John Doe", 50000, 30),
                    createEmployee("5255f1a5-f9f7-4be5-829a-134bde088d17", "Jane Smith", 60000, 35));

            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(employees, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            List<Employee> result = employeeService.getAllEmployees();

            assertEquals(2, result.size());
            assertEquals("John Doe", result.get(0).getName());
            verify(restTemplate, times(1))
                    .exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("should return empty list when no employees exist")
        void shouldReturnEmptyListWhenNoEmployees() {
            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(Collections.emptyList(), "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            List<Employee> result = employeeService.getAllEmployees();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getEmployeesByNameSearch")
    class GetEmployeesByNameSearchTests {

        @Test
        @DisplayName("should find employees by name fragment")
        void shouldFindEmployeesByNameFragment() {
            List<Employee> employees = Arrays.asList(
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "John Doe", 50000, 30),
                    createEmployee("5255f1a5-f9f7-4be5-829a-134bde088d17", "Jane Smith", 60000, 35),
                    createEmployee("6a3a170b-22cd-4ac2-aad1-9bb5b34a1508", "Johnny Bravo", 55000, 28));

            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(employees, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            List<Employee> result = employeeService.getEmployeesByNameSearch("John");

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(e -> e.getName().toLowerCase().contains("john")));
        }

        @Test
        @DisplayName("should perform case-insensitive search")
        void shouldPerformCaseInsensitiveSearch() {
            List<Employee> employees =
                    Arrays.asList(createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "JOHN DOE", 50000, 30));

            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(employees, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            List<Employee> result = employeeService.getEmployeesByNameSearch("john");

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("should return employee when found")
        void shouldReturnEmployeeWhenFound() {
            String id = "4a3a170b-22cd-4ac2-aad1-9bb5b34a1507";
            Employee employee = createEmployee(id, "John Doe", 50000, 30);

            ApiResponse<Employee> apiResponse = new ApiResponse<>(employee, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<Employee>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL + "/" + id),
                            eq(HttpMethod.GET),
                            isNull(),
                            any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            Employee result = employeeService.getEmployeeById(id);

            assertEquals("John Doe", result.getName());
            assertEquals(50000, result.getSalary());
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when employee not found")
        void shouldThrowExceptionWhenNotFound() {
            String id = "non-existent-id";

            when(restTemplate.exchange(
                            eq(BASE_URL + "/" + id),
                            eq(HttpMethod.GET),
                            isNull(),
                            any(ParameterizedTypeReference.class)))
                    .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

            assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(id));
        }
    }

    @Nested
    @DisplayName("getHighestSalary")
    class GetHighestSalaryTests {

        @Test
        @DisplayName("should return highest salary")
        void shouldReturnHighestSalary() {
            List<Employee> employees = Arrays.asList(
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "John Doe", 50000, 30),
                    createEmployee("5255f1a5-f9f7-4be5-829a-134bde088d17", "Jane Smith", 80000, 35),
                    createEmployee("6a3a170b-22cd-4ac2-aad1-9bb5b34a1508", "Bob Wilson", 65000, 40));

            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(employees, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            Integer result = employeeService.getHighestSalary();

            assertEquals(80000, result);
        }

        @Test
        @DisplayName("should return zero when no employees")
        void shouldReturnZeroWhenNoEmployees() {
            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(Collections.emptyList(), "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            Integer result = employeeService.getHighestSalary();

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("getTop10HighestEarningEmployeeNames")
    class GetTop10HighestEarningEmployeeNamesTests {

        @Test
        @DisplayName("should return top 10 earners sorted by salary")
        void shouldReturnTop10EarnersSortedBySalary() {
            List<Employee> employees = Arrays.asList(
                    createEmployee("1a3a170b-22cd-4ac2-aad1-9bb5b34a1501", "Emp1", 10000, 30),
                    createEmployee("2a3a170b-22cd-4ac2-aad1-9bb5b34a1502", "Emp2", 20000, 30),
                    createEmployee("3a3a170b-22cd-4ac2-aad1-9bb5b34a1503", "Emp3", 30000, 30),
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1504", "Emp4", 40000, 30),
                    createEmployee("5a3a170b-22cd-4ac2-aad1-9bb5b34a1505", "Emp5", 50000, 30),
                    createEmployee("6a3a170b-22cd-4ac2-aad1-9bb5b34a1506", "Emp6", 60000, 30),
                    createEmployee("7a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "Emp7", 70000, 30),
                    createEmployee("8a3a170b-22cd-4ac2-aad1-9bb5b34a1508", "Emp8", 80000, 30),
                    createEmployee("9a3a170b-22cd-4ac2-aad1-9bb5b34a1509", "Emp9", 90000, 30),
                    createEmployee("aa3a170b-22cd-4ac2-aad1-9bb5b34a150a", "Emp10", 100000, 30),
                    createEmployee("ba3a170b-22cd-4ac2-aad1-9bb5b34a150b", "Emp11", 110000, 30),
                    createEmployee("ca3a170b-22cd-4ac2-aad1-9bb5b34a150c", "Emp12", 120000, 30));

            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(employees, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            List<String> result = employeeService.getTop10HighestEarningEmployeeNames();

            assertEquals(10, result.size());
            assertEquals("Emp12", result.get(0));
            assertEquals("Emp11", result.get(1));
            assertEquals("Emp3", result.get(9));
        }

        @Test
        @DisplayName("should return all if less than 10 employees")
        void shouldReturnAllIfLessThan10Employees() {
            List<Employee> employees = Arrays.asList(
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "John", 50000, 30),
                    createEmployee("5255f1a5-f9f7-4be5-829a-134bde088d17", "Jane", 80000, 35));

            ApiResponse<List<Employee>> apiResponse =
                    new ApiResponse<>(employees, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            List<String> result = employeeService.getTop10HighestEarningEmployeeNames();

            assertEquals(2, result.size());
            assertEquals("Jane", result.get(0));
            assertEquals("John", result.get(1));
        }
    }

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployeeTests {

        @Test
        @DisplayName("should create employee successfully")
        void shouldCreateEmployeeSuccessfully() {
            CreateEmployeeInput input = CreateEmployeeInput.builder()
                    .name("New Employee")
                    .salary(55000)
                    .age(28)
                    .title("Software Engineer")
                    .build();

            Employee createdEmployee =
                    createEmployee("4a3a170b-22cd-4ac2-aad1-9bb5b34a1507", "New Employee", 55000, 28);
            ApiResponse<Employee> apiResponse =
                    new ApiResponse<>(createdEmployee, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<Employee>> responseEntity = ResponseEntity.ok(apiResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);

            Employee result = employeeService.createEmployee(input);

            assertEquals("New Employee", result.getName());
            assertEquals(55000, result.getSalary());
        }
    }

    @Nested
    @DisplayName("deleteEmployeeById")
    class DeleteEmployeeByIdTests {

        @Test
        @DisplayName("should delete employee and return name")
        void shouldDeleteEmployeeAndReturnName() {
            String id = "4a3a170b-22cd-4ac2-aad1-9bb5b34a1507";
            Employee employee = createEmployee(id, "John Doe", 50000, 30);

            ApiResponse<Employee> getResponse = new ApiResponse<>(employee, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<Employee>> getResponseEntity = ResponseEntity.ok(getResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL + "/" + id),
                            eq(HttpMethod.GET),
                            isNull(),
                            any(ParameterizedTypeReference.class)))
                    .thenReturn(getResponseEntity);

            ApiResponse<Boolean> deleteResponse = new ApiResponse<>(true, "Successfully processed request.", null);
            ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = ResponseEntity.ok(deleteResponse);

            when(restTemplate.exchange(
                            eq(BASE_URL),
                            eq(HttpMethod.DELETE),
                            any(HttpEntity.class),
                            any(ParameterizedTypeReference.class)))
                    .thenReturn(deleteResponseEntity);

            String result = employeeService.deleteEmployeeById(id);

            assertEquals("John Doe", result);
        }
    }

    @Nested
    @DisplayName("Retry Logic")
    class RetryLogicTests {

        @Test
        @DisplayName("should throw exception after max retries exceeded on rate limiting")
        void shouldThrowExceptionAfterMaxRetriesOnRateLimiting() {
            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, null, null));

            assertThrows(EmployeeApiException.class, () -> employeeService.getAllEmployees());

            verify(restTemplate, times(3))
                    .exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("should throw exception after max retries exceeded on server error")
        void shouldThrowExceptionAfterMaxRetriesOnServerError() {
            when(restTemplate.exchange(
                            eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null));

            assertThrows(EmployeeApiException.class, () -> employeeService.getAllEmployees());

            verify(restTemplate, times(3))
                    .exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
        }
    }
}
