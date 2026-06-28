package com.example.agregador_vagas_estagio_backend.service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.interfaces.VagaScraper;
import com.microsoft.playwright.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GupyService implements VagaScraper{

    @Override
    public List<VagaDTO> retornaVagas(String termo, String local) {
        List<VagaDTO> listaDeVagas = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            String urlBase = "https://portal.gupy.io/job-search/term=%s";

            String url = String.format(urlBase, termo, termo.length());

            page.navigate(url);

            page.waitForSelector("main#main-content ul li", new Page.WaitForSelectorOptions().setTimeout(10000));

            String html = page.content();
            Document doc = Jsoup.parse(html);

            Elements jobs = doc.select("main#main-content ul li");

            for (Element job : jobs) {
                
                Element linkElement = job.selectFirst("a");
                String link = (linkElement != null) ? linkElement.attr("href") : "";

                
                Element tituloElement = job.selectFirst("h3");
                String titulo = (tituloElement != null) ? tituloElement.text() : "Título não encontrado";

                
                Element empresaElement = job.selectFirst("div[aria-label^=Empresa]");
                String empresa = "Não informada";
                if (empresaElement != null) {
                    empresa = empresaElement.attr("aria-label").replace("Empresa ", "").trim();
                }

               
                Element localElement = job.selectFirst("span[data-testid='job-location']");
                String localizacao = (localElement != null) ? localElement.text() : "Não informada";

                if (!titulo.equals("Título não encontrado") && !link.isEmpty()) {
                    VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, link, "Gupy");
                    listaDeVagas.add(vaga);
                    
                }
            }

            
            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaDeVagas;
    }
}