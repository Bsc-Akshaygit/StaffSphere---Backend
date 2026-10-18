package com.studentlearn.learnplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studentlearn.learnplatform.configuration.JwtFilter;
import com.studentlearn.learnplatform.dto.*;
import com.studentlearn.learnplatform.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private SignupRequest makeSignupRequest(String name, String email, String password) {
        SignupRequest r = new SignupRequest();
        r.setName(name);
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    private AddEmpRequest makeAddEmpRequest() {
        AddEmpRequest r = new AddEmpRequest();
        r.setName("John Doe");
        r.setEmail("john@example.com");
        r.setPhone(9876543210L);
        r.setAadhaar(123456789012L);
        r.setDateOfBirth(LocalDate.of(1995, 4, 12));
        r.setJoingDate(LocalDate.of(2022, 8, 1));
        r.setDesignation("Engineer");
        r.setSalary(75000f);
        r.setAddress("Delhi");
        return r;
    }

    private ViewEmployee makeViewEmployee(String name, String email) {
        return new ViewEmployee(
                name, email, 9876543210L, 123456789012L,
                LocalDate.of(1995, 4, 12),
                LocalDate.of(2022, 8, 1),
                "Engineer", 75000f, "Delhi"
        );
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    void home_ShouldReturn200WithWelcomeMessage() throws Exception {
        mockMvc.perform(get("/platform/home"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to learn platform"));
    }

    @Test
    @WithMockUser
    void addEmployee_ValidRequestWithJwt_ShouldReturn200() throws Exception {
        when(userService.addEmployee(any())).thenReturn(ResponseEntity.ok("Employee Added"));

        mockMvc.perform(post("/platform/addEmployee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(makeAddEmpRequest())))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee Added"));
    }

    @Test
    void addEmployee_WithoutJwt_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/platform/addEmployee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(makeAddEmpRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void addEmployee_BlankName_ShouldReturn400WithValidationError() throws Exception {
        AddEmpRequest request = makeAddEmpRequest();
        request.setName("");

        mockMvc.perform(post("/platform/addEmployee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name")));
    }

    @Test
    @WithMockUser
    void addEmployee_InvalidEmail_ShouldReturn400WithValidationError() throws Exception {
        AddEmpRequest request = makeAddEmpRequest();
        request.setEmail("bad-email");

        mockMvc.perform(post("/platform/addEmployee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("email")));
    }

    @Test
    @WithMockUser
    void addEmployee_NegativeSalary_ShouldReturn400WithValidationError() throws Exception {
        AddEmpRequest request = makeAddEmpRequest();
        request.setSalary(-1000f);

        mockMvc.perform(post("/platform/addEmployee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("salary")));
    }

    @Test
    @WithMockUser
    void viewEmployee_WithJwt_ShouldReturn200WithEmployeeList() throws Exception {
        List<ViewEmployee> employees = List.of(
                makeViewEmployee("John Doe", "john@example.com"),
                makeViewEmployee("Jane Doe", "jane@example.com")
        );
        when(userService.viewEmployee()).thenReturn(employees);

        mockMvc.perform(get("/platform/viewEmployee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"));
    }

    @Test
    void viewEmployee_WithoutJwt_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/platform/viewEmployee"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void viewEmployee_NoEmployees_ShouldReturnEmptyList() throws Exception {
        when(userService.viewEmployee()).thenReturn(List.of());

        mockMvc.perform(get("/platform/viewEmployee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void deleteEmployee_ValidParams_ShouldReturn200() throws Exception {
        when(userService.deleteEmployee("john@example.com", 123456789012L))
                .thenReturn(ResponseEntity.ok("Employee Deleted Successfully"));

        mockMvc.perform(delete("/platform/deleteEmployee")
                        .with(csrf())
                        .param("email", "john@example.com")
                        .param("aadhaar", "123456789012"))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee Deleted Successfully"));
    }

    @Test
    void deleteEmployee_WithoutJwt_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/platform/deleteEmployee")
                        .with(csrf())
                        .param("email", "john@example.com")
                        .param("aadhaar", "123456789012"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deleteEmployee_EmployeeNotFound_ShouldReturn400() throws Exception {
        when(userService.deleteEmployee(anyString(), anyLong()))
                .thenThrow(new RuntimeException("Employee not found or does not belong to you"));

        mockMvc.perform(delete("/platform/deleteEmployee")
                        .with(csrf())
                        .param("email", "nobody@example.com")
                        .param("aadhaar", "999999999999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Employee not found or does not belong to you"));
    }

    @Test
    @WithMockUser
    void deleteEmployee_MissingEmailParam_ShouldReturn400() throws Exception {
        mockMvc.perform(delete("/platform/deleteEmployee")
                        .with(csrf())
                        .param("aadhaar", "123456789012"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteEmployee_MissingAadhaarParam_ShouldReturn400() throws Exception {
        mockMvc.perform(delete("/platform/deleteEmployee")
                        .with(csrf())
                        .param("email", "john@example.com"))
                .andExpect(status().isBadRequest());
    }
}
