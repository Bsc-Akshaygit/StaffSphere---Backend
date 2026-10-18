package com.studentlearn.learnplatform.service;

import com.studentlearn.learnplatform.entity.RefreshToken;
import com.studentlearn.learnplatform.entity.UserEntity;
import com.studentlearn.learnplatform.repository.RefreshTokenRepo;
import com.studentlearn.learnplatform.repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private final UserRepo  userRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final long REFRESH_TOKEN_DURATION = 1000 * 60 * 60 * 24 * 7;

    public RefreshTokenService(UserRepo userRepo,  RefreshTokenRepo refreshTokenRepo) {
        this.userRepo = userRepo;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    public RefreshToken createRefreshToken(String email) {
        UserEntity userEntity = userRepo.findByEmail(email).orElseThrow(()-> new RuntimeException("User with email " + email + " not found"));
        refreshTokenRepo.deleteByUserEntity(userEntity);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserEntity(userEntity);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpieryDate(new Date(System.currentTimeMillis()+REFRESH_TOKEN_DURATION));
        return refreshTokenRepo.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(()-> new RuntimeException("RefreshToken not found"));
        if(refreshToken.getExpieryDate().before(new Date())) {
            refreshTokenRepo.delete(refreshToken);
            throw new RuntimeException("RefreshToken is expired");
        }
        return refreshToken;
    }
    public void deleteByUser(String email){
        UserEntity user = userRepo.findByEmail(email).orElseThrow(()-> new RuntimeException("User with email " + email + " not found"));
        refreshTokenRepo.deleteByUserEntity(user);
    }


}
