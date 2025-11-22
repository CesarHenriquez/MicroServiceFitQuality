package com.microservicio.registrousuario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.microservicio.registrousuario.model.Usuario;
import com.microservicio.registrousuario.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario crearUsuario(Usuario usuario) {
        // La encriptación se maneja aquí antes de guardar
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    
    public Usuario buscarPorNickname(String nickname) {
        return usuarioRepository.findByNickname(nickname)
                .orElse(null); 
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNickname(usuarioActualizado.getNickname());
            usuario.setCorreo(usuarioActualizado.getCorreo());
            usuario.setRol(usuarioActualizado.getRol());
            
            if (usuarioActualizado.getClave() != null && !usuarioActualizado.getClave().trim().isEmpty()) {
                usuario.setClave(passwordEncoder.encode(usuarioActualizado.getClave()));
            }
            
            return usuarioRepository.save(usuario);
        }).orElse(null);
    }
}