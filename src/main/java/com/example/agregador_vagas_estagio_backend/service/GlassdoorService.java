package com.example.agregador_vagas_estagio_backend.service;

import java.io.IOException;
import java.util.ArrayList; // Import necessário para a lista
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service; // Não esqueça do @Service aqui também!

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.interfaces.VagaScraper;

@Service // Permite que o Spring injete o GlassdoorService lá no seu AgregadorService
public class GlassdoorService implements VagaScraper{

    @Override
    public List<VagaDTO> retornaVagas(String termo, String local, int pagina) {
        // 1. Cria a lista vazia que vai guardar os DTOs
        List<VagaDTO> listaDeVagas = new ArrayList<>();

        try {  
            String urlBase = "https://www.glassdoor.com.br/Vaga/%s-vagas-SRCH_IL.0,6_IN36_KO0,%d.htm";

            // O String.format substitui o primeiro %s por 'termo' e o %d pelo tamanho do termo
            String url = String.format(urlBase, termo, termo.length());

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .get();
                    
            Element jobsListUl = doc.selectFirst("ul[aria-label=Jobs List]");

            if (jobsListUl != null) {
                Elements jobs = jobsListUl.select("li[data-test=jobListing]");
                
                for (Element job : jobs) {
                    // --- PARSEANDO CADA INFO INDIVIDUAL ---
                    Element titleElement = job.selectFirst("a[data-test=job-title]");
                    String titulo = (titleElement != null) ? titleElement.text() : "Não encontrado";
                    String linkVaga = (titleElement != null) ? titleElement.attr("href") : "";
                    
                    Element employerElement = job.selectFirst("div[id^=job-employer]");
                    String empresa = (employerElement != null) ? employerElement.text() : "Não encontrado";
                    
                    Element locationElement = job.selectFirst("div[id^=job-location]");
                    String localizacao = (locationElement != null) ? locationElement.text() : "Não encontrado";
                    if (!local.isBlank()) {
                        if (!localizacao.toLowerCase().contains(local.toLowerCase())) {
                            continue;
                        }
                    }

                    // 2. O MAPPER NA PRÁTICA: Cria o objeto DTO com as Strings que limpamos do HTML
                    // (Se o seu VagaDTO for um 'record', use: new VagaDTO(titulo, empresa, localizacao, linkVaga, "Glassdoor"))
                    VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, linkVaga, "Glassdoor");
                    
                    // 3. Adiciona o DTO mapeado na nossa lista
                    listaDeVagas.add(vaga);
                }
            }

            // 4. Retorna a lista cheia de DTOs
            return listaDeVagas;

        } catch (IOException e) {
            e.printStackTrace(); 
            // 5. Se der erro, retorna a lista vazia (melhor do que estourar null no controller)
            return listaDeVagas; 
        }
    }
}