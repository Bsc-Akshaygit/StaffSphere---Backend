package com.studentlearn.learnplatform.controller;

import com.studentlearn.learnplatform.dto.*;
import com.studentlearn.learnplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platform")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home() {
        return "Welcome to learn platform";
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest signupRequest) {
        String response = userService.signup(signupRequest);
        if (response.equals("User Registration Successful")) {
            return ResponseEntity.ok()
                    .body(response);
        }else  {
            return ResponseEntity.badRequest().body("Registration Failed");
        }
    }



    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse authunticated = userService.login(loginRequest);
        if(authunticated!=null){
            return ResponseEntity.ok().body(authunticated);
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refreshToken(
            @RequestBody RefreshTokenRequest request) {

        String accessToken =
                userService.refreshAccessToken(
                        request.getRefreshToken()
                );

        return new RefreshTokenResponse(accessToken);
    }

    @PostMapping("addEmployee")
    public ResponseEntity<String> addEmployee(@Valid @RequestBody AddEmpRequest request) {
        return userService.addEmployee(request);
    }

    @GetMapping("/viewEmployee")
    public List<ViewEmployee> viewEmployee(){
        return userService.viewEmployee();
    }

    @DeleteMapping("/deleteEmployee")
    public ResponseEntity<String> deleteEmployee(
            @RequestParam String email,
            @RequestParam long aadhaar) {
        return userService.deleteEmployee(email, aadhaar);
    }

}
