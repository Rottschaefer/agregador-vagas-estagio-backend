package com.example.agregador_vagas_estagio_backend.service;

import java.io.IOException;
import java.util.ArrayList; 
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service; 

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.interfaces.VagaScraper;

@Service // Permite que o Spring injete o InfojobsService lá no seu AgregadorService
public class InfojobsService implements VagaScraper{

    @Override
    public List<VagaDTO> retornaVagas(String termo, String local, int pagina) {
        List<VagaDTO> listaDeVagas = new ArrayList<>();

        try {  
            String urlBase = "https://www.infojobs.com.br/empregos.aspx?palabra=%s&provincia=182";

            String url = String.format(urlBase, termo);

            Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .get();
            
            Element jobsListUl = doc.selectFirst("div.js_vacanciesGridFragment.mb-16");

            if (jobsListUl != null) {
                Elements jobs = jobsListUl.select("div.card.js_rowCard");
                
                for (Element job : jobs) {
                    Element titleElement = job.selectFirst("h2.js_vacancyTitle");
                    String titulo = (titleElement != null) ? titleElement.text() : "Não encontrado";

                    // "job" já é a div principal que possui o data-href
                    String linkVaga = "";
                    if (titleElement != null && titleElement.parent() != null){
                        String href = titleElement.parent().attr("href");
                        linkVaga = "https://www.infojobs.com.br" + href;

                    }
                    else{
                        linkVaga = "Não encontrado";
                    }
                    
                    Element employerElement = job.selectFirst("div.align-items-baseline a");
                    String empresa = (employerElement != null) ? employerElement.text() : "Não encontrado";
                    
                    Element locationElement = job.selectFirst("div.mb-8");
                    String localizacao = (locationElement != null) ? locationElement.ownText() : "Não encontrado";
                    if (!local.isBlank()) {
                        if (!localizacao.toLowerCase().contains(local.toLowerCase())) {
                            continue;
                        }
                    }

                   
                    VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, linkVaga, "Infojobs");
                    
                    listaDeVagas.add(vaga);
                }
            }

            return listaDeVagas;

        } catch (IOException e) {
            e.printStackTrace(); 
            return listaDeVagas; 
        }
    }
}