package com.jpdictionary.auth.security;

import com.jpdictionary.auth.entity.User;
import com.jpdictionary.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Thiếu tên đăng nhập hoặc email");
        }
        String cleanIdentifier = identifier.trim();
        User user = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(cleanIdentifier, cleanIdentifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản với định danh: " + cleanIdentifier
                ));
        return UserPrincipal.from(user);
    }
}

