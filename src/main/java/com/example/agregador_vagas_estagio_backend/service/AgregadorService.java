package com.example.agregador_vagas_estagio_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;

@Service
public class AgregadorService {

    @Autowired
    private GlassdoorService glassdoorService;

    @Autowired
    private InfojobsService infojobsService;

    @Autowired
    private AcademiaUniversitarioService academiaUniversitarioService;

    @Autowired
    private GupyService gupyService;

    public List<VagaDTO> agregaVagas(String termo, String local){
        List<VagaDTO> vagas = new ArrayList<>();
        vagas.addAll(glassdoorService.retornaVagas(termo, local));
        vagas.addAll(infojobsService.retornaVagas(termo, local));
        vagas.addAll(academiaUniversitarioService.retornaVagas(termo, local));
        vagas.addAll(gupyService.retornaVagas(termo, local));
        return vagas;
    }
    
}
