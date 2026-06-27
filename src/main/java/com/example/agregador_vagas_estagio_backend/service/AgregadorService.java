package com.example.agregador_vagas_estagio_backend.service;

features-allan
import java.util.ArrayList; //  importado para criar uma lista nova


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;

@Service
public class AgregadorService {

    @Autowired
    private GlassdoorService glassdoorService;

    @AutoWired
    private InfojobsService infojobsService;

    @Autowired
    private AcademiaUniversitarioService academiaUniversitarioService;

    @Autowired
    private GupyService gupyService;
    
    @AutoWired
    private EstagiarBrService estagiarBrService; 

    public List<VagaDTO> agregaVagas(String termo, String local){
        List<VagaDTO> vagas = new ArrayList<>();
        vagas.addAll(glassdoorService.retornaVagas(termo, local));
        vagas.addAll(infojobsService.retornaVagas(termo, local));
        vagas.addAll(academiaUniversitarioService.retornaVagas(termo, local));
        vagas.addAll(gupyService.retornaVagas(termo, local));
        vagas.addAll(estagiarBrService.retornaVagas(termo, local));
      
        return vagas;
    }
    
}