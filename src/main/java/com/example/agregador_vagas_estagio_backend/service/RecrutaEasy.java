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
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

@Service("recrutaeasyService") 
public class RecrutaEasy implements VagaScraper {

    @Override
    public List<VagaDTO> retornaVagas(String termo, String local, int pagina) {
        List<VagaDTO> listaDeVagas = new ArrayList<>();

        pagina -= 1; //Recruta Easy começa a contagem em 0

        try (Playwright playwright = Playwright.create()){  
            String urlBase = "https://recrutaeasy.com.br/vagas?search=%s&pag=%d&state=RJ";
            String url = String.format(urlBase, termo, pagina);

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            page.navigate(url);

            page.waitForLoadState(LoadState.NETWORKIDLE);

        
            String htmlRenderizado = page.content();
            browser.close();

            Document doc = Jsoup.parse(htmlRenderizado);

            Elements jobs = doc.select("article.relative.bg-white.shadow-xl");



            for (Element job : jobs) {

                
                Element titleElement = job.selectFirst("h1"); 
                String titulo = (titleElement != null) ? titleElement.text() : "Não encontrado";
                
                Element employerElement = job.selectFirst("h1 + p"); 
                String empresa = (employerElement != null) ? employerElement.text() : "Não encontrado";
                
                Elements infoSpans = job.select("span[class='flex items-center gap-1.5']");

                String localizacao = "Não encontrado";

                if (!infoSpans.isEmpty()) {
                    localizacao = infoSpans.first().text(); 
                }

                Element linkElement = job.selectFirst("a[href]"); 
                String linkVaga = (linkElement != null) ? linkElement.attr("href") : "";
                
                if (linkVaga.startsWith("/")) {
                    linkVaga = "https://recrutaeasy.com.br" + linkVaga;
                }

                VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, linkVaga, "RecrutaEasy");
                
                listaDeVagas.add(vaga);
            }

            return listaDeVagas;

        } catch (Exception e) { 
  
            System.err.println("Erro ao realizar scraping com Playwright: " + e.getMessage());
            return listaDeVagas; 
        }
    }
}