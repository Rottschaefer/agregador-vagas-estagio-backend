package com.example.agregador_vagas_estagio_backend.service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.interfaces.VagaScraper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("academiauniversitarioService") //As fontes passam todas pra minúsculo, aí sem essa linha daria erro
public class AcademiaUniversitarioService implements VagaScraper{

    @Override
    public List<VagaDTO> retornaVagas(String termo, String local, int pagina) {
        List<VagaDTO> listaDeVagas = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            page.navigate("https://app.academiadouniversitario.com.br/");

    
            Locator inputBusca = page.locator("input[name='job']");

            inputBusca.fill(termo);

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Buscar minha vaga")).first().click();
            
            page.waitForSelector("div[data-testid='jobs-list-grid']", new Page.WaitForSelectorOptions().setTimeout(5000));

            //Espera as requisições da página acabarem antes de procurar as tags
            page.waitForLoadState(LoadState.NETWORKIDLE);

            String htmlRenderizado = page.content();
            Document doc = Jsoup.parse(htmlRenderizado);

            Element jobsListDiv = doc.selectFirst("div[data-testid=jobs-list-grid]");

            if (jobsListDiv != null) {
                Elements jobs = jobsListDiv.select("article");
                
            for (Element job : jobs) {
                
                Elements h3 = job.select("h3");
                
                Element empresaElement = h3.get(0);
                String empresa = (empresaElement != null) ? empresaElement.text() : "Não informada";


                Element tituloElement = h3.get(1); 
                String titulo = (tituloElement != null) ? tituloElement.text() : "Título não encontrado";

                 Element localizacaoElement = job.selectFirst("span:contains(SP), span:contains(RJ)"); 
                 String localizacao = (localizacaoElement != null) ? localizacaoElement.text() : "Remoto/Não informado";
                 if (!local.isBlank()) {
                    if (!localizacao.toLowerCase().contains(local.toLowerCase())) {
                        continue;
                    }
                 }

                 Element linkElement = job.selectFirst("a[href^=/vaga/]"); 
                 if (linkElement == null) {
                     linkElement = job.selectFirst("a"); 
                 }
                 String linkRelativo = (linkElement != null) ? linkElement.attr("href") : "";
                 String link = !linkRelativo.isEmpty() ? "https://app.academiadouniversitario.com.br" + linkRelativo : "";

                 VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, link, "Academia do Universitário");
                
                 listaDeVagas.add(vaga);
            }
            } else {
                System.out.println("Não foi possível encontrar o container 'jobs-list-grid'.");
            }

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaDeVagas;
    }
}