package com.studentlearn.learnplatform.repository;

import com.studentlearn.learnplatform.entity.Employee;
import com.studentlearn.learnplatform.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee,Integer> {
    List<Employee> findByUser(UserEntity user);
    Optional<Employee> findByEmailAndAadhaarAndUser(String email, long aadhaar, UserEntity user);
}
