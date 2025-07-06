package com.example.userservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "roles")
@Getter
@Setter
public class Role extends BaseModel{
    @Column(length = 50, nullable = false, unique = true)
    private String roleName;
}
