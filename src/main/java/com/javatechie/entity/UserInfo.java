package com.javatechie.entity;

import java.util.UUID;

import jakarta.persistence.Entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    @Id
    private String id = UUID.randomUUID().toString();

    private String username;

    private String email;

    private String password;

    private String roles;
}
