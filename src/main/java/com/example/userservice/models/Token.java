package com.example.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Temporal;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Token extends BaseModel{
    private String token;

    @ManyToOne
    private User user;

    private boolean isExpired;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiredAt;
}
