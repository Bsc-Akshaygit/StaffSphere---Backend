package com.studentlearn.learnplatform.service;

import com.studentlearn.learnplatform.dto.*;
import com.studentlearn.learnplatform.entity.Employee;
import com.studentlearn.learnplatform.entity.RefreshToken;
import com.studentlearn.learnplatform.entity.UserEntity;
import com.studentlearn.learnplatform.exception.EmailAlreadyExistException;
import com.studentlearn.learnplatform.jwt.JwtToken;
import com.studentlearn.learnplatform.repository.EmployeeRepo;
import com.studentlearn.learnplatform.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtToken jwtToken;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private EmployeeRepo employeeRepo;

    @InjectMocks
    private UserService userService;

    private UserEntity makeUser(Long id, String name, String email) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword("encoded-password");
        return u;
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

    private Employee makeEmployee(String name, String email, long aadhaar, UserEntity owner) {
        Employee e = new Employee();
        e.setName(name);
        e.setEmail(email);
        e.setPhone(9876543210L);
        e.setAadhaar(aadhaar);
        e.setDateOfBirth(LocalDate.of(1995, 4, 12));
        e.setJoingDate(LocalDate.of(2022, 8, 1));
        e.setDesignation("Engineer");
        e.setSalary(75000f);
        e.setAddress("Delhi");
        e.setUser(owner);
        return e;
    }

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void signup_NewEmail_ShouldSaveUserAndReturnSuccessMessage() {
        SignupRequest request = makeSignupRequest("Alice", "alice@example.com", "password123");

        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        String result = userService.signup(request);

        assertEquals("User Registration Successful", result);
        verify(userRepo, times(1)).save(any(UserEntity.class));
    }

    @Test
    void signup_ExistingEmail_ShouldThrowEmailAlreadyExistException() {
        SignupRequest request = makeSignupRequest("Alice", "alice@example.com", "password123");
        when(userRepo.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(makeUser(1L, "Alice", "alice@example.com")));

        assertThrows(EmailAlreadyExistException.class, () -> userService.signup(request));
        verify(userRepo, never()).save(any());
    }

    @Test
    void signup_ShouldEncodePasswordBeforeSaving() {
        SignupRequest request = makeSignupRequest("Bob", "bob@example.com", "rawpassword");

        when(userRepo.findByEmail("bob@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawpassword")).thenReturn("hashed");

        userService.signup(request);

        verify(passwordEncoder, times(1)).encode("rawpassword");
    }

    @Test
    void login_ValidCredentials_ShouldReturnLoginResponseWithTokens() {
        LoginRequest request = new LoginRequest("alice@example.com", "password123");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtToken.generateToken("alice@example.com")).thenReturn("access-token-xyz");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-xyz");
        when(refreshTokenService.createRefreshToken("alice@example.com")).thenReturn(refreshToken);

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("access-token-xyz", response.getAccess_token());
        assertEquals("refresh-token-xyz", response.getRefresh_token());
    }

    @Test
    void login_InvalidCredentials_ShouldThrowBadCredentialsException() {
        LoginRequest request = new LoginRequest("alice@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.login(request));
    }

    @Test
    void login_ValidCredentials_ShouldCallGenerateToken() {
        LoginRequest request = new LoginRequest("alice@example.com", "password123");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtToken.generateToken("alice@example.com")).thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken("alice@example.com")).thenReturn(refreshToken);

        userService.login(request);

        verify(jwtToken, times(1)).generateToken("alice@example.com");
        verify(refreshTokenService, times(1)).createRefreshToken("alice@example.com");
    }


    @Test
    void refreshAccessToken_ValidToken_ShouldReturnNewAccessToken() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-xyz");
        refreshToken.setUserEntity(user);

        when(refreshTokenService.verifyRefreshToken("refresh-token-xyz")).thenReturn(refreshToken);
        when(jwtToken.generateToken("alice@example.com")).thenReturn("new-access-token");

        String result = userService.refreshAccessToken("refresh-token-xyz");

        assertEquals("new-access-token", result);
    }

    @Test
    void refreshAccessToken_ExpiredToken_ShouldThrowRuntimeException() {
        when(refreshTokenService.verifyRefreshToken("expired-token"))
                .thenThrow(new RuntimeException("RefreshToken is expired"));

        assertThrows(RuntimeException.class, () -> userService.refreshAccessToken("expired-token"));
    }

    @Test
    void refreshAccessToken_NonExistentToken_ShouldThrowRuntimeException() {
        when(refreshTokenService.verifyRefreshToken("ghost-token"))
                .thenThrow(new RuntimeException("RefreshToken not found"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.refreshAccessToken("ghost-token"));
        assertEquals("RefreshToken not found", ex.getMessage());
    }


    @Test
    void addEmployee_ValidRequest_ShouldSaveEmployeeAndReturn200() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userService.addEmployee(makeAddEmpRequest());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Employee Added", response.getBody());
        verify(employeeRepo, times(1)).save(any(Employee.class));
    }

    @Test
    void addEmployee_ShouldAssociateEmployeeWithLoggedInUser() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        userService.addEmployee(makeAddEmpRequest());

        verify(employeeRepo).save(argThat(emp -> emp.getUser().equals(user)));
    }

    @Test
    void addEmployee_UserNotFoundInDB_ShouldThrowRuntimeException() {
        mockSecurityContext("ghost@example.com");
        when(userRepo.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.addEmployee(makeAddEmpRequest()));
        verify(employeeRepo, never()).save(any());
    }

    @Test
    void viewEmployee_ShouldReturnOnlyLoggedInUsersEmployees() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Employee emp1 = makeEmployee("John", "john@example.com", 123456789012L, user);
        Employee emp2 = makeEmployee("Jane", "jane@example.com", 987654321098L, user);
        when(employeeRepo.findByUser(user)).thenReturn(List.of(emp1, emp2));

        List<ViewEmployee> result = userService.viewEmployee();

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Jane", result.get(1).getName());
    }

    @Test
    void viewEmployee_NoEmployees_ShouldReturnEmptyList() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(employeeRepo.findByUser(user)).thenReturn(List.of());

        List<ViewEmployee> result = userService.viewEmployee();

        assertTrue(result.isEmpty());
    }

    @Test
    void viewEmployee_ShouldNeverCallFindAll() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(employeeRepo.findByUser(user)).thenReturn(List.of());

        userService.viewEmployee();

        verify(employeeRepo, never()).findAll();
    }

    @Test
    void deleteEmployee_ValidEmailAndAadhaar_ShouldDeleteAndReturn200() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Employee emp = makeEmployee("John", "john@example.com", 123456789012L, user);
        when(employeeRepo.findByEmailAndAadhaarAndUser("john@example.com", 123456789012L, user))
                .thenReturn(Optional.of(emp));

        ResponseEntity<String> response = userService.deleteEmployee("john@example.com", 123456789012L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Employee Deleted Successfully", response.getBody());
        verify(employeeRepo, times(1)).delete(emp);
    }

    @Test
    void deleteEmployee_EmployeeNotFound_ShouldThrowRuntimeException() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(employeeRepo.findByEmailAndAadhaarAndUser(anyString(), anyLong(), any()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.deleteEmployee("nobody@example.com", 999999999999L));

        assertEquals("Employee not found or does not belong to you", ex.getMessage());
        verify(employeeRepo, never()).delete(any());
    }

    @Test
    void deleteEmployee_WrongAadhaar_ShouldThrowRuntimeException() {
        UserEntity user = makeUser(1L, "Alice", "alice@example.com");
        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(employeeRepo.findByEmailAndAadhaarAndUser("john@example.com", 000000000000L, user))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.deleteEmployee("john@example.com", 000000000000L));
        verify(employeeRepo, never()).delete(any());
    }

    @Test
    void deleteEmployee_ShouldOnlyDeleteEmployeeBelongingToLoggedInUser() {
        UserEntity userA = makeUser(1L, "Alice", "alice@example.com");
        UserEntity userB = makeUser(2L, "Bob", "bob@example.com");

        mockSecurityContext("alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(userA));

        when(employeeRepo.findByEmailAndAadhaarAndUser("john@example.com", 123456789012L, userA))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.deleteEmployee("john@example.com", 123456789012L));
        verify(employeeRepo, never()).delete(any());
    }
}
