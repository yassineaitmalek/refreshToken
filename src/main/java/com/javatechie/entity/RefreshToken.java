package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String token;

    private boolean isLoggedOut;

    private Instant expiryDate;

    private String userId;

}
