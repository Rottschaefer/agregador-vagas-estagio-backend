package com.example.agregador_vagas_estagio_backend.service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.interfaces.VagaScraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AgregadorService {

    String[] fontes = {
        "Gupy",
        "Glassdoor",
        "InfoJobs",
        "EstagiarBR",
        "AcademiaUniversitario"
    };

    // Injeta todos os services que implementam VagaScraper aqui automaticamente
    @Autowired
    private Map<String, VagaScraper> scrapers;

    //Busca apenas na fonte especificada na URL
    public List<VagaDTO> buscaPorFonte(String fonte, String termo, String local) {
        // Monta o nome do Bean esperado (Ex: se vier "gupy", vira "gupyService")
        System.out.print(fonte);

        if (fonte == "") return buscaVagas(termo, local);
        
        String beanName = fonte.toLowerCase() + "Service";
        
        VagaScraper scraper = scrapers.get(beanName);
        
        if (scraper == null) {
            throw new IllegalArgumentException("Fonte de vagas inválida: " + fonte);
        }
        
        return scraper.retornaVagas(termo, local);
    }

    public List<VagaDTO> buscaVagas(String termo, String local) {
        // Monta o nome do Bean esperado (Ex: se vier "gupy", vira "gupyService")
        
        List<VagaDTO> vagas = new ArrayList<>();
        
        for (String fonte : fontes) {
            String beanName = fonte.toLowerCase() + "Service";

            VagaScraper scraper = scrapers.get(beanName);

            if (scraper == null) {
                throw new IllegalArgumentException("Fonte de vagas inválida: " + fonte);
            }

            try {
                vagas.addAll(scraper.retornaVagas(termo, local));
            } catch (Exception e) {
                System.out.println("Erro em " + fonte);
                e.printStackTrace();
            }

        }
        
        Collections.shuffle(vagas);
        
        return vagas;
    }
        
}