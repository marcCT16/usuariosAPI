package com.example.practica_api_rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica_api_rest.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

}
