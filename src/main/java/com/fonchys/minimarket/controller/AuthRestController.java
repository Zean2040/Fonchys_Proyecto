package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.dto.LoginRequestDTO;
import com.fonchys.minimarket.dto.LoginResponseDTO;
import com.fonchys.minimarket.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login de la API REST: valida credenciales contra la misma capa de Spring Security usada
 * por la web (DaoAuthenticationProvider + IUsuarioService) y devuelve un JWT en vez de
 * abrir una sesion con cookie.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String rol = userDetails.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("ROLE_CAJERO")
            .replace("ROLE_", "");

        String token = jwtUtil.generarToken(userDetails.getUsername(), rol);
        return ResponseEntity.ok(new LoginResponseDTO(token, "Bearer", userDetails.getUsername(), rol, jwtUtil.getExpirationMs()));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponseDTO> yo(@AuthenticationPrincipal UserDetails userDetails) {
        String rol = userDetails.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("ROLE_CAJERO")
            .replace("ROLE_", "");
        return ResponseEntity.ok(new LoginResponseDTO(null, "Bearer", userDetails.getUsername(), rol, 0));
    }
}
