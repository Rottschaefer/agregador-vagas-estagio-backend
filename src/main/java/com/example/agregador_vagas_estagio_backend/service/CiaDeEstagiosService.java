package com.example.agregador_vagas_estagio_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.interfaces.VagaScraper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

@Service("ciadeestagiosService")
public class CiaDeEstagiosService implements VagaScraper {

    @Override
    public List<VagaDTO> retornaVagas(String termo, String local, int pagina) {
        List<VagaDTO> listaDeVagas = new ArrayList<>();

        // 1. Inicia o motor do Playwright
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            String url = "https://app.ciadeestagios.com.br/vagas-publicas";
            page.navigate(url);

            // 2. O SEGREDO: Espera pelo menos um <md-card> aparecer na tela por até 10 segundos
            try {
                page.waitForSelector("md-card", new Page.WaitForSelectorOptions().setTimeout(10000));
                page.waitForLoadState(LoadState.NETWORKIDLE); // Garante que as requisições API terminaram
            } catch (Exception e) {
                System.out.println("Timeout: Os cards da Cia de Estágios não carregaram a tempo.");
                browser.close();
                return listaDeVagas;
            }

            // 3. Agora que a página está renderizada com o JavaScript, pegamos o HTML completo
            String htmlRenderizado = page.content();
            Document doc = Jsoup.parse(htmlRenderizado);


            // 4. Fazemos a busca dos elementos usando o Jsoup em cima do HTML renderizado
            Elements jobs = doc.select("md-card");



            for (Element job : jobs) {
                try {
                    // --- CAPTURANDO OS DADOS INTERNOS DO CARD ---
                    
                    // 1. Título da vaga: <span class="md-title">
                    Element titleElement = job.selectFirst("span.md-title");
                    String titulo = (titleElement != null) ? titleElement.text().trim() : "Título não encontrado";

                    System.out.println(titulo);
                    
                    // 2. Empresa: <span class="md-subhead">
                    Element empresaElement = job.selectFirst("span.md-subhead");
                    String empresa = (empresaElement != null) ? empresaElement.text().trim() : "Não informada";

                    // 3. Localização: O PRIMEIRO <p> dentro de <md-card-content>
                    Element locationElement = job.selectFirst("md-card-content p");
                    String localizacao = (locationElement != null) ? locationElement.text().trim() : "Não informada";

                    // 4. Link: Continua vindo do atributo ng-click do md-card pai
                    String ngClick = job.attr("ng-click"); 
                    String linkVaga = "";
                    // if (ngClick.contains("openVaga(")) {
                    //     linkVaga = ngClick.replace("openVaga(", "").replace(")", "").replace("'", "").trim();
                    //     if (!linkVaga.startsWith("http")) {
                    //         linkVaga = "https://app.ciadeestagios.com.br" + linkVaga;
                    //     }
                    // }

                    // Filtro por localidade do seu front
                    if (!local.isBlank()) {
                        if (!localizacao.toLowerCase().contains(local.toLowerCase())) {
                            continue;
                        }
                    }

                    System.out.println(titulo + empresa);

                    // Cria o DTO mapeado e adiciona na lista
                    VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, linkVaga, "Cia de Estágios");
                    listaDeVagas.add(vaga);

                        


                } catch (Exception ex) {
                    // Ignora falhas em cards individuais quebrados e continua rodando o loop
                    ex.printStackTrace();
                }
            }
            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaDeVagas;
    }
}