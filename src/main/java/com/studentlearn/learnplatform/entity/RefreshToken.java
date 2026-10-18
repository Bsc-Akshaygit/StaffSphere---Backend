package com.studentlearn.learnplatform.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private Date expieryDate;

    @OneToOne
    @JoinColumn(name="user_id")
    private UserEntity userEntity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpieryDate() {
        return expieryDate;
    }

    public void setExpieryDate(Date expieryDate) {
        this.expieryDate = expieryDate;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
}
