package com.example.agregador_vagas_estagio_backend.service;

import java.util.ArrayList; //  importado para criar uma lista nova
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;

@Service
public class AgregadorService {

    @Autowired
    private GlassdoorService glassdoorService;

    @Autowired
    private EstagiarBrService estagiarBrService; 

    public List<VagaDTO> agregaVagas(String termo, String local){
        // foi criada uma lista única para comportar as vagas de todos os sites
        List<VagaDTO> vagas = new ArrayList<>();
        vagas.addAll(glassdoorService.retornaVagas(termo, local));
        vagas.addAll(estagiarBrService.retornaVagas(termo, local));
        
        return vagas;
    }
    
}