package com.example.agregador_vagas_estagio_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.service.AgregadorService;

@CrossOrigin("http://localhost:5173")
@RestController
@RequestMapping("buscar-vagas")
public class BuscaVagas {
    
    @Autowired
    private AgregadorService agregadorService;

    public BuscaVagas(AgregadorService agregadorService) {
        this.agregadorService = agregadorService;
    }

    @GetMapping()
    public List<VagaDTO> buscar(
            @RequestParam(name = "fonte") String fonte,
            @RequestParam(name = "termo") String termo,
            @RequestParam(name = "local", required = false, defaultValue = "Brasil") String local) {
        
        return agregadorService.buscaPorFonte(fonte, termo, local);
    }


}