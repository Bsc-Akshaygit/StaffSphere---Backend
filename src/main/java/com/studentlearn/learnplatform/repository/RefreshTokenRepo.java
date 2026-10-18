package com.studentlearn.learnplatform.repository;

import com.studentlearn.learnplatform.entity.RefreshToken;
import com.studentlearn.learnplatform.entity.UserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String refreshToken);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userEntity = :user")
    void deleteByUserEntity(@Param("user") UserEntity user);

}
