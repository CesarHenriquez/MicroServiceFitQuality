package com.microservicio.autenticarusuario.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Collections; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.microservicio.autenticarusuario.service.AuthService;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class) 
public class AuthControllerTest {

    @Mock 
    private AuthService authService; 

    @InjectMocks 
    private AuthController authController; 

    private final String NICKNAME = "testuser";
    private final String CLAVE = "password123";
    private final String TOKEN_MOCK = "generated.mock.token.12345";
    private Map<String, String> credentials;

    @BeforeEach
    void setUp() {
        credentials = Map.of("nickname", NICKNAME, "clave", CLAVE);
    }

    
    @Test
    void testLogin_Exitoso_RetornaTokenYStatusOk() {
        
        when(authService.autenticar(NICKNAME, CLAVE)).thenReturn(Mono.just(TOKEN_MOCK));

        
        Mono<ResponseEntity<String>> responseMono = authController.login(credentials);
        
       
        ResponseEntity<String> response = responseMono.block();

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TOKEN_MOCK, response.getBody());

        verify(authService, times(1)).autenticar(NICKNAME, CLAVE);
    }

    
    @Test
    void testLogin_CredencialesInvalidas_RetornaUnauthorized() {
        final String ERROR_MESSAGE = "Credenciales inválidas.";
        
       
        when(authService.autenticar(NICKNAME, CLAVE)).thenReturn(Mono.just(ERROR_MESSAGE));

        Mono<ResponseEntity<String>> responseMono = authController.login(credentials);
        ResponseEntity<String> response = responseMono.block();

      
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ERROR_MESSAGE, response.getBody());
        
        verify(authService, times(1)).autenticar(NICKNAME, CLAVE);
    }

   
    @Test
    void testLogin_UsuarioNoEncontrado_RetornaUnauthorized() {
        final String ERROR_MESSAGE = "Usuario no encontrado.";
       
        when(authService.autenticar(NICKNAME, CLAVE)).thenReturn(Mono.just(ERROR_MESSAGE));

        Mono<ResponseEntity<String>> responseMono = authController.login(credentials);
        ResponseEntity<String> response = responseMono.block();

       
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ERROR_MESSAGE, response.getBody());
        
        verify(authService, times(1)).autenticar(NICKNAME, CLAVE);
    }
    
  
    @Test
    void testLogin_CredencialesNulas_RetornaUnauthorized() {
     
        final String ERROR_MESSAGE = "Error: falta la clave."; 
        
      
        when(authService.autenticar(NICKNAME, null)).thenReturn(Mono.just(ERROR_MESSAGE));
        
       
        Map<String, String> incompleteCredentials = Collections.singletonMap("nickname", NICKNAME);

        Mono<ResponseEntity<String>> responseMono = authController.login(incompleteCredentials);
        ResponseEntity<String> response = responseMono.block();

       
        assertNotNull(response);
       
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()); 
    }
}