package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.user.LoginRequest;
import org.example.dto.request.user.RegisterRequest;
import org.example.dto.request.user.Role;
import org.example.dto.response.user.auth.AuthResponse;
import org.example.entity.Group;
import org.example.entity.StudentGroup;
import org.example.entity.StudentGroupId;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.exception.NotUniqueObjectException;
import org.example.mapper.UserMapper;
import org.example.repository.GroupRepository;
import org.example.repository.StudentGroupRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new NotUniqueObjectException("The user with this email already exists");
        }
        if (request.getRole() == Role.TEACHER) {
            request.setStudentGroup("");
        }
        var user = userMapper.fromRegisterRequest(request);
        userRepository.save(user);
        linkStudentToGroup(user);

        var claims = new HashMap<String, Object>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole() == null ? null : user.getRole().name());
        var token = jwtService.generateToken(claims, user);
        return AuthResponse.builder()
                .accessToken(token)
                .expiresInMs(jwtService.getJwtExpiration())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("The user with this email was not found"));

        var claims = new HashMap<String, Object>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole() == null ? null : user.getRole().name());
        String token = jwtService.generateToken(claims, user);
        return AuthResponse.builder()
                .accessToken(token)
                .expiresInMs(jwtService.getJwtExpiration())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    private void linkStudentToGroup(User user) {
        if (user.getRole() != org.example.entity.Role.STUDENT) {
            return;
        }
        String groupCode = user.getStudentGroup();
        if (groupCode == null || groupCode.isBlank()) {
            return;
        }
        groupRepository.findByActiveTrue().stream()
                .filter(g -> groupCode.equalsIgnoreCase(g.getName()))
                .findFirst()
                .ifPresent(group -> saveStudentGroupMembership(user, group));
    }

    private void saveStudentGroupMembership(User student, Group group) {
        StudentGroupId id = new StudentGroupId(student.getId(), group.getId());
        if (studentGroupRepository.existsById(id)) {
            return;
        }
        StudentGroup membership = new StudentGroup();
        membership.setId(id);
        membership.setStudent(student);
        membership.setGroup(group);
        studentGroupRepository.save(membership);
    }
}
