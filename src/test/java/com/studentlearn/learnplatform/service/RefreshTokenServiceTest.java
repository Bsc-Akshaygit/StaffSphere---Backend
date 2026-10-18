package com.studentlearn.learnplatform.service;

import com.studentlearn.learnplatform.entity.RefreshToken;
import com.studentlearn.learnplatform.entity.UserEntity;
import com.studentlearn.learnplatform.repository.RefreshTokenRepo;
import com.studentlearn.learnplatform.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private RefreshTokenRepo refreshTokenRepo;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UserEntity makeUser(Long id, String email) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setEmail(email);
        u.setName("Test User");
        u.setPassword("encoded");
        return u;
    }

    private RefreshToken makeToken(UserEntity user, boolean expired) {
        RefreshToken token = new RefreshToken();
        token.setToken("test-uuid-token");
        token.setUserEntity(user);
        // expired = 1 hour ago, valid = 7 days from now
        long time = expired
                ? System.currentTimeMillis() - (1000 * 60 * 60)
                : System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7);
        token.setExpieryDate(new Date(time));
        return token;
    }


    @Test
    void createRefreshToken_ValidEmail_ShouldReturnSavedToken() {
        UserEntity user = makeUser(1L, "alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken("alice@example.com");

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(user, result.getUserEntity());
    }

    @Test
    void createRefreshToken_ShouldDeleteExistingTokenBeforeSavingNew() {
        UserEntity user = makeUser(1L, "alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        refreshTokenService.createRefreshToken("alice@example.com");

        InOrder order = inOrder(refreshTokenRepo);
        order.verify(refreshTokenRepo).deleteByUserEntity(user);
        order.verify(refreshTokenRepo).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_ShouldGenerateUniqueTokenEachTime() {
        UserEntity user = makeUser(1L, "alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken first = refreshTokenService.createRefreshToken("alice@example.com");
        RefreshToken second = refreshTokenService.createRefreshToken("alice@example.com");

        assertNotEquals(first.getToken(), second.getToken());
    }

    @Test
    void createRefreshToken_ShouldSetExpiryInFuture() {
        UserEntity user = makeUser(1L, "alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken("alice@example.com");

        assertTrue(result.getExpieryDate().after(new Date()));
    }

    @Test
    void createRefreshToken_ShouldSetExpiryApproximately7DaysFromNow() {
        UserEntity user = makeUser(1L, "alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken("alice@example.com");

        long sevenDaysMs = 1000L * 60 * 60 * 24 * 7;
        long diff = result.getExpieryDate().getTime() - System.currentTimeMillis();

        assertTrue(diff > sevenDaysMs - 5000 && diff <= sevenDaysMs);
    }

    @Test
    void createRefreshToken_UserNotFound_ShouldThrowRuntimeException() {
        when(userRepo.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refreshTokenService.createRefreshToken("ghost@example.com"));

        assertTrue(ex.getMessage().contains("ghost@example.com"));
        verify(refreshTokenRepo, never()).save(any());
    }

    @Test
    void createRefreshToken_ShouldSaveTokenLinkedToCorrectUser() {
        UserEntity user = makeUser(1L, "alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        refreshTokenService.createRefreshToken("alice@example.com");

        verify(refreshTokenRepo).save(captor.capture());
        assertEquals(user, captor.getValue().getUserEntity());
    }

    @Test
    void verifyRefreshToken_ValidNonExpiredToken_ShouldReturnToken() {
        UserEntity user = makeUser(1L, "alice@example.com");
        RefreshToken token = makeToken(user, false);
        when(refreshTokenRepo.findByToken("test-uuid-token")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.verifyRefreshToken("test-uuid-token");

        assertNotNull(result);
        assertEquals("test-uuid-token", result.getToken());
    }

    @Test
    void verifyRefreshToken_ValidToken_ShouldNotDeleteIt() {
        UserEntity user = makeUser(1L, "alice@example.com");
        RefreshToken token = makeToken(user, false);
        when(refreshTokenRepo.findByToken("test-uuid-token")).thenReturn(Optional.of(token));

        refreshTokenService.verifyRefreshToken("test-uuid-token");

        verify(refreshTokenRepo, never()).delete(any(RefreshToken.class));
    }

    @Test
    void verifyRefreshToken_ExpiredToken_ShouldDeleteItFromDB() {
        UserEntity user = makeUser(1L, "alice@example.com");
        RefreshToken token = makeToken(user, true);
        when(refreshTokenRepo.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class,
                () -> refreshTokenService.verifyRefreshToken("expired-token"));

        verify(refreshTokenRepo, times(1)).delete(token);
    }

    @Test
    void verifyRefreshToken_ExpiredToken_ShouldThrowWithCorrectMessage() {
        UserEntity user = makeUser(1L, "alice@example.com");
        RefreshToken token = makeToken(user, true);
        when(refreshTokenRepo.findByToken("expired-token")).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refreshTokenService.verifyRefreshToken("expired-token"));

        assertEquals("RefreshToken is expired", ex.getMessage());
    }

    @Test
    void verifyRefreshToken_NonExistentToken_ShouldThrowRuntimeException() {
        when(refreshTokenRepo.findByToken("ghost-token")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refreshTokenService.verifyRefreshToken("ghost-token"));

        assertEquals("RefreshToken not found", ex.getMessage());
    }

    @Test
    void verifyRefreshToken_NonExistentToken_ShouldNeverCallDelete() {
        when(refreshTokenRepo.findByToken("ghost-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> refreshTokenService.verifyRefreshToken("ghost-token"));

        verify(refreshTokenRepo, never()).delete(any());
    }

    @Test
    void deleteByUser_ExistingUser_ShouldCallDeleteByUserEntity() {
        UserEntity user = makeUser(1L, "alice@example.com");
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        refreshTokenService.deleteByUser("alice@example.com");

        verify(refreshTokenRepo, times(1)).deleteByUserEntity(user);
    }

    @Test
    void deleteByUser_NonExistentUser_ShouldThrowRuntimeException() {
        when(userRepo.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> refreshTokenService.deleteByUser("ghost@example.com"));

        verify(refreshTokenRepo, never()).deleteByUserEntity(any());
    }
}
