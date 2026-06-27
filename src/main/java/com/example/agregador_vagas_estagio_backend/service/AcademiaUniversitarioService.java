package com.example.agregador_vagas_estagio_backend.service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AcademiaUniversitarioService {

    public List<VagaDTO> retornaVagas(String termo, String local) {
        List<VagaDTO> listaDeVagas = new ArrayList<>();

        // 1. Inicializa o ambiente do Playwright (roda em modo headless por padrão)
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            // 2. Navega até a página inicial de vagas
            page.navigate("https://app.academiadouniversitario.com.br/");

            // 3. Interage com o input de busca (Identifique pelo placeholder ou id no F12)
            // Aqui usei o seletor baseado no placeholder visível no seu print
            page.locator("ul']");

            // 4. Clica no botão laranja de buscar
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Buscar minha vaga")).first().click();
            // 5. Espera crucial: aguarda os novos cards de vaga carregarem na árvore do HTML
            // Altere o seletor abaixo para a classe ou elemento real que representa o container ou o card da vaga
            page.waitForSelector("div[data-testid='jobs-list-grid']", new Page.WaitForSelectorOptions().setTimeout(5000));

            // 6. O COMBO PERFEITO: Pega o HTML totalmente renderizado pelo JavaScript
            String htmlRenderizado = page.content();
            Document doc = Jsoup.parse(htmlRenderizado);

            // // 1. Captura a div container da lista de vagas usando o data-testid
            // Element jobsListDiv = doc.selectFirst("div[data-testid=jobs-list-grid]");

            // if (jobsListDiv != null) {
            //     // 2. Busca todas as tags <article> que estão dentro dessa div (cada uma é uma vaga)
            //     Elements jobs = jobsListDiv.select("article");
                
            //     // Supondo que você já fez: Elements jobs = doc.select("article");
            // for (Element job : jobs) {
                
                
            //     Element empresaElement = job.selectFirst("h3");
            //     String empresa = (empresaElement != null) ? empresaElement.text() : "Não informada";

            //     Element tituloElement = job.selectFirst("h2"); 
            //     String titulo = (tituloElement != null) ? tituloElement.text() : "Título não encontrado";

            //     Element localizacaoElement = job.selectFirst("span:contains(SP), span:contains(RJ)"); 
            //     String localizacao = (localizacaoElement != null) ? localizacaoElement.text() : "Remoto/Não informado";

               
            //     Element linkElement = job.selectFirst("a[href^=/vaga/]"); 
            //     if (linkElement == null) {
            //         linkElement = job.selectFirst("a"); 
            //     }
            //     String linkRelativo = (linkElement != null) ? linkElement.attr("href") : "";
            //     String link = !linkRelativo.isEmpty() ? "https://app.academiadouniversitario.com.br" + linkRelativo : "";

            //     VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, link, "Academia do Universitário");
                
            //     listaDeVagas.add(vaga);
            // }
            // } else {
            //     System.out.println("Não foi possível encontrar o container 'jobs-list-grid'.");
            // }

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaDeVagas;
    }
}