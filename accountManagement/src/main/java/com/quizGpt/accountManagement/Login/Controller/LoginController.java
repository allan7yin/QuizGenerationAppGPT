package com.quizGpt.accountManagement.Login.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quizGpt.accountManagement.Config.Security.JwtUtils;
import com.quizGpt.accountManagement.Config.Security.UserDetailsImpl;
import com.quizGpt.accountManagement.Login.Dto.JwtResponseDto;
import com.quizGpt.accountManagement.Login.Dto.LoginRequestDto;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/account")
@AllArgsConstructor
public class LoginController {
    AuthenticationManager authenticationManager;
        
    private JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginRequestDto, @RequestHeader String IdToken, Authentication authentication) {
        
        // we are taking this in since there is a JWT filter which will first intecept this response, reaching here means it passed, cand can just return an ok

         // getting here means FireBase has authentiated the token, or, the jwt was validated by ourselves. Regardless, return JWT 
         UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
         String jwt = jwtUtils.generateJwt(authentication);

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDto(jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles));
    }

    @PostMapping("/auth")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequestDto) {
        logger.info("LOGIN REQUEST: Beginning auth process.");

        Authentication authentication = null;

        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword()));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwt(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // try and using a stream. Come back if this does not work
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDto(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }
}
