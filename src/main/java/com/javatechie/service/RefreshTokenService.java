package com.javatechie.service;

import com.javatechie.dto.AuthRequest;
import com.javatechie.dto.JwtResponse;
import com.javatechie.dto.RefreshTokenRequest;
import com.javatechie.entity.RefreshToken;
import com.javatechie.entity.UserInfo;
import com.javatechie.repository.RefreshTokenRepository;
import com.javatechie.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    public Optional<RefreshToken> createRefreshToken(String userId) {

        return Optional.ofNullable(userId)
                .map(e -> RefreshToken.builder()
                        .userId(e)
                        .token(UUID.randomUUID().toString())
                        .expiryDate(Instant.now().plusMillis(600000))// 10
                        .build())
                .map(e -> Optional.of(refreshTokenRepository.save(e)))
                .orElse(Optional.empty());

    }

    public Optional<RefreshToken> getByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Optional<RefreshToken> verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(
                    token.getToken() + " Refresh token was expired. Please make a new signin request");
        }
        return Optional.of(token);
    }

    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return getByToken(refreshTokenRequest.getRefreshToken())
                .map(this::verifyExpiration)
                .filter(e -> e.isPresent())
                .map(e -> e.get())
                .map(RefreshToken::getUserId)
                .map(userId -> jwtService.generateToken(userId))
                .map(accessToken -> JwtResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshTokenRequest.getRefreshToken())
                        .build())
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    public JwtResponse logIn(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            return userInfoRepository.findByUsername(authRequest.getUsername())
                    .map(this::generateJWT)
                    .orElseThrow(() -> new UsernameNotFoundException("invalid user request ! "));

        } else {
            throw new UsernameNotFoundException("invalid user request !");
        }
    }

    public void logOut(RefreshToken refreshToken) {
        verifyExpiration(refreshToken)
                .ifPresent(e -> {
                    e.setLoggedOut(true);
                    refreshTokenRepository.save(e);
                });

    }

    public JwtResponse generateJWT(UserInfo user) {
        return createRefreshToken(user.getId())
                .map(e -> JwtResponse.builder()
                        .accessToken(jwtService.generateToken(user.getId()))
                        .refreshToken(e.getToken()).build())
                .orElseThrow(() -> new UsernameNotFoundException("invalid user request !"));

    }
}
