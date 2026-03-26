package org.example.mapper.utils;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Named("UserMapperUtil")
@Component
@RequiredArgsConstructor
public class UserMapperUtil {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Named("getEncodedPassword")
    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);

    }
}
