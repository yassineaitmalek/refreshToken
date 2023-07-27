package com.javatechie.config;

import com.javatechie.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserInfoUserDetailsService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    @Override
    public UserInfoUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .map(UserInfoUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));

    }

    public UserInfoUserDetails loadUserById(String userId) throws UsernameNotFoundException {
        return repository.findById(userId)
                .map(UserInfoUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("user with id " + userId + " not found "));

    }
}
