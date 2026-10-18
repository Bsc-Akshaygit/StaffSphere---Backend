package com.studentlearn.learnplatform.service;

import com.studentlearn.learnplatform.entity.UserEntity;
import com.studentlearn.learnplatform.repository.UserRepo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo  userRepo;
    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        UserEntity user = userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()

        );
    }
}
