package com.studentlearn.learnplatform.service;

import com.studentlearn.learnplatform.dto.*;
import com.studentlearn.learnplatform.entity.Employee;
import com.studentlearn.learnplatform.entity.RefreshToken;
import com.studentlearn.learnplatform.entity.UserEntity;
import com.studentlearn.learnplatform.exception.EmailAlreadyExistException;
import com.studentlearn.learnplatform.jwt.JwtToken;
import com.studentlearn.learnplatform.repository.EmployeeRepo;
import com.studentlearn.learnplatform.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final JwtToken jwtToken;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmployeeRepo employeeRepo;

    public UserService(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtToken jwtToken,
            RefreshTokenService refreshTokenService,
            EmployeeRepo employeeRepo
    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtToken = jwtToken;
        this.refreshTokenService = refreshTokenService;
        this.employeeRepo = employeeRepo;
    }

    // =========================
    // SIGNUP
    // =========================
    public String signup(SignupRequest signupRequest) {

        if (userRepo.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException(
                    "Email Already Exist. try another one"
            );
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setName(signupRequest.getName());
        userEntity.setEmail(signupRequest.getEmail());
        userEntity.setPassword(
                passwordEncoder.encode(signupRequest.getPassword())
        );

        userRepo.save(userEntity);

        return "User Registration Successful";
    }

    // =========================
    // LOGIN
    // =========================
    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                );

        if (authentication.isAuthenticated()) {

            String accessToken =
                    jwtToken.generateToken(loginRequest.getEmail());

            RefreshToken refreshToken =
                    refreshTokenService.createRefreshToken(
                            loginRequest.getEmail()
                    );

            return new LoginResponse(
                    accessToken,
                    refreshToken.getToken()
            );
        }

        return null;
    }

    // =========================
    // REFRESH ACCESS TOKEN
    // =========================
    public String refreshAccessToken(String refreshToken) {

        RefreshToken token =
                refreshTokenService.verifyRefreshToken(refreshToken);

        String email =
                token.getUserEntity().getEmail();

        return jwtToken.generateToken(email);
    }

    // =========================
    // ADD EMPLOYEE
    // =========================
    public ResponseEntity<String> addEmployee(AddEmpRequest request) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        UserEntity currentUser =
                userRepo.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Authenticated user not found"
                                )
                        );

        Employee emp = new Employee();

        emp.setName(request.getName());
        emp.setEmail(request.getEmail());
        emp.setAadhaar(request.getAadhaar());
        emp.setDateOfBirth(request.getDateOfBirth());
        emp.setAddress(request.getAddress());
        emp.setDesignation(request.getDesignation());
        emp.setJoingDate(request.getJoingDate());
        emp.setPhone(request.getPhone());
        emp.setSalary(request.getSalary());

        // Associate employee with logged-in user
        emp.setUser(currentUser);

        employeeRepo.save(emp);

        return ResponseEntity.ok("Employee Added");
    }

    // =========================
    // VIEW EMPLOYEES
    // =========================
    public List<ViewEmployee> viewEmployee() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        UserEntity currentUser =
                userRepo.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Authenticated user not found"
                                )
                        );

        return employeeRepo.findByUser(currentUser)
                .stream()
                .map(employee -> new ViewEmployee(
                        employee.getName(),
                        employee.getEmail(),
                        employee.getPhone(),
                        employee.getAadhaar(),
                        employee.getDateOfBirth(),
                        employee.getJoingDate(),
                        employee.getDesignation(),
                        employee.getSalary(),
                        employee.getAddress()
                ))
                .toList();
    }

    // =========================
    // DELETE EMPLOYEE
    // =========================
    public ResponseEntity<String> deleteEmployee(
            String email,
            long aadhaar
    ) {

        String currentEmail =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        UserEntity currentUser =
                userRepo.findByEmail(currentEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Authenticated user not found"
                                )
                        );

        Employee employee =
                employeeRepo
                        .findByEmailAndAadhaarAndUser(
                                email,
                                aadhaar,
                                currentUser
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found or does not belong to you"
                                )
                        );

        System.out.println("===== DELETING EMPLOYEE =====");
        System.out.println("Name        : " + employee.getName());
        System.out.println("Email       : " + employee.getEmail());
        System.out.println("Phone       : " + employee.getPhone());
        System.out.println("Aadhaar     : " + employee.getAadhaar());
        System.out.println("Date of Birth : " + employee.getDateOfBirth());
        System.out.println("Joining Date  : " + employee.getJoingDate());
        System.out.println("Designation : " + employee.getDesignation());
        System.out.println("Salary      : " + employee.getSalary());
        System.out.println("Address     : " + employee.getAddress());
        System.out.println("Deleted by  : " + currentEmail);
        System.out.println("=============================");

        employeeRepo.delete(employee);

        return ResponseEntity.ok(
                "Employee Deleted Successfully"
        );
    }
}
