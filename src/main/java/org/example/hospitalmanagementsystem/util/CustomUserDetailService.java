package org.example.hospitalmanagementsystem.util;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.repository.RegisterRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final RegisterRepository registerRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Register register = registerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new User(
                register.getUsername(),
                register.getPassword(),
                List.of(new SimpleGrantedAuthority(register.getRole()))
        );
    }
}
