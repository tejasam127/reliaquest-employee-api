package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;

import com.reliaquest.api.controller.EmployeeController;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class ApiApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("EmployeeController bean should be available")
    void employeeControllerBeanExists() {
        assertNotNull(applicationContext.getBean(EmployeeController.class));
    }

    @Test
    @DisplayName("EmployeeService bean should be available")
    void employeeServiceBeanExists() {
        assertNotNull(applicationContext.getBean(EmployeeService.class));
    }
}
