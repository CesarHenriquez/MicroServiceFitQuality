package com.microservicio.autenticarusuario.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.microservicio.autenticarusuario.client.UsuarioClient;
import com.microservicio.autenticarusuario.model.Rol;
import com.microservicio.autenticarusuario.model.Usuario;
import com.microservicio.autenticarusuario.util.JwtUtil;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier; 

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioCliente;
    private final String NICKNAME = "cliente1";
    private final String CLAVE_SIN_ENCRIPTAR = "clave123";
    private final String CLAVE_ENCRIPTADA = "$2a$10$encodedhash";
    private final String TOKEN_GENERADO = "mock.jwt.token";

    @BeforeEach
    void setUp() {
        usuarioCliente = new Usuario(1L, NICKNAME, CLAVE_ENCRIPTADA, "cliente@mail.com", new Rol(1L, "CLIENTE"));
    }

    
    @Test
    void testAutenticar_CredencialesCorrectas_EmiteToken() {
       
        when(usuarioClient.obtenerUsuarioPorNickname(NICKNAME)).thenReturn(Mono.just(usuarioCliente));
        
        
        when(passwordEncoder.matches(CLAVE_SIN_ENCRIPTAR, CLAVE_ENCRIPTADA)).thenReturn(true);

        
        when(jwtUtil.generateToken(usuarioCliente)).thenReturn(TOKEN_GENERADO);

        Mono<String> resultadoMono = authService.autenticar(NICKNAME, CLAVE_SIN_ENCRIPTAR);

        StepVerifier.create(resultadoMono)
                .expectNext(TOKEN_GENERADO)
                .verifyComplete();
        
        verify(usuarioClient, times(1)).obtenerUsuarioPorNickname(NICKNAME);
        verify(jwtUtil, times(1)).generateToken(usuarioCliente);
    }

   
    @Test
    void testAutenticar_UsuarioNoEncontrado_EmiteMensaje() {
       
        when(usuarioClient.obtenerUsuarioPorNickname(anyString())).thenReturn(Mono.empty());

        Mono<String> resultadoMono = authService.autenticar("unknown", CLAVE_SIN_ENCRIPTAR);

        
        StepVerifier.create(resultadoMono)
                .expectNext("Usuario no encontrado.")
                .verifyComplete();
        
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

   
    @Test
    void testAutenticar_ClaveIncorrecta_EmiteMensajeDeError() {
        when(usuarioClient.obtenerUsuarioPorNickname(NICKNAME)).thenReturn(Mono.just(usuarioCliente));
        
        
        when(passwordEncoder.matches(CLAVE_SIN_ENCRIPTAR, CLAVE_ENCRIPTADA)).thenReturn(false);

        Mono<String> resultadoMono = authService.autenticar(NICKNAME, CLAVE_SIN_ENCRIPTAR);

       
        StepVerifier.create(resultadoMono)
                .expectNext("Credenciales inválidas.")
                .verifyComplete();
        
        verify(jwtUtil, never()).generateToken(any(Usuario.class));
    }

    
    @Test
    void testAutenticar_ErrorDeComunicacion_EmiteMensajeDeError() {
       
        when(usuarioClient.obtenerUsuarioPorNickname(NICKNAME)).thenReturn(Mono.error(new RuntimeException("Error de red")));

        Mono<String> resultadoMono = authService.autenticar(NICKNAME, CLAVE_SIN_ENCRIPTAR);

        
        StepVerifier.create(resultadoMono)
                .expectNextMatches(msg -> msg.startsWith("Error al autenticar:"))
                .verifyComplete();
    }
}
