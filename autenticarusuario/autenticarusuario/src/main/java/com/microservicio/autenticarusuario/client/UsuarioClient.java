package com.microservicio.autenticarusuario.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.microservicio.autenticarusuario.model.Usuario;

import reactor.core.publisher.Mono;

@Component
public class UsuarioClient {
    private final WebClient webClient;

    public UsuarioClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://localhost:8020/usuarios") 
                .build();
    }

    public Mono<Usuario> obtenerUsuarioPorNickname(String nickname) {
    return webClient.get()
        .uri("/interno/nickname/{nickname}", nickname)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
            response -> response.bodyToMono(String.class)
                .flatMap(error -> Mono.error(new RuntimeException("Error al consultar usuario: " + error))))
        .bodyToMono(Usuario.class);
    }   

}
