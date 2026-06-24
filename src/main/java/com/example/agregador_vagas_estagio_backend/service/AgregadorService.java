package com.example.agregador_vagas_estagio_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;

@Service
public class AgregadorService {

    @Autowired
    private GlassdoorService glassdoorService;

    public List<VagaDTO> agregaVagas(String termo, String local){
        return glassdoorService.retornaVagas(termo, local);
    }
    
}
