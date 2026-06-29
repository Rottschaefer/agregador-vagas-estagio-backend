package com.example.agregador_vagas_estagio_backend.interfaces;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import java.util.List;

public interface VagaScraper {
    List<VagaDTO> retornaVagas(String termo, String local, int pagina);
}