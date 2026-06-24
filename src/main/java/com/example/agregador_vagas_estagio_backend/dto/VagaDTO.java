package com.example.agregador_vagas_estagio_backend.dto;

public record VagaDTO(
    String titulo, 
    String empresa, 
    String localizacao, 
    String link, 
    String fonte // "Gupy", "Glassdoor", etc.
) {}
