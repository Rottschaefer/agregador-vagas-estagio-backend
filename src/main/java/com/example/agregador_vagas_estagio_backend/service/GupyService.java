package com.example.agregador_vagas_estagio_backend.service;

import com.example.agregador_vagas_estagio_backend.dto.VagaDTO;
import com.example.agregador_vagas_estagio_backend.interfaces.VagaScraper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GupyService implements VagaScraper {
    private static final int VAGAS_POR_PAGINA = 10;
    private static final int MAX_PAGINAS_ORIGINAIS_POR_BUSCA = 20;

    @Override
    public List<VagaDTO> retornaVagas(String termo, String local, int pagina) {
        List<VagaDTO> listaDeVagas = new ArrayList<>();
        String localFiltro = (local != null) ? local : "";
        int vagasParaIgnorar = Math.max(0, (pagina - 1) * VAGAS_POR_PAGINA);
        int vagasIgnoradas = 0;
        int paginasSemResultado = 0;

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            for (int paginaOriginal = 1; paginaOriginal <= MAX_PAGINAS_ORIGINAIS_POR_BUSCA; paginaOriginal++) {
                if (listaDeVagas.size() >= VAGAS_POR_PAGINA) {
                    break;
                }

                List<VagaDTO> vagasDaPaginaOriginal = buscarVagasNaPaginaOriginal(page, termo, localFiltro, paginaOriginal);

                if (vagasDaPaginaOriginal.isEmpty()) {
                    paginasSemResultado++;
                    if (paginasSemResultado >= 2) {
                        break;
                    }
                    continue;
                }

                paginasSemResultado = 0;

                for (VagaDTO vaga : vagasDaPaginaOriginal) {
                    if (vagasIgnoradas < vagasParaIgnorar) {
                        vagasIgnoradas++;
                        continue;
                    }

                    listaDeVagas.add(vaga);

                    if (listaDeVagas.size() >= VAGAS_POR_PAGINA) {
                        break;
                    }
                }
            }

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaDeVagas;
    }

    private List<VagaDTO> buscarVagasNaPaginaOriginal(Page page, String termo, String local, int paginaOriginal) {
        List<VagaDTO> vagas = new ArrayList<>();
        String urlBase = "https://portal.gupy.io/job-search/term=%s?page=%d";
        String url = String.format(urlBase, termo, paginaOriginal);

        try {
            page.navigate(url);
            page.waitForSelector("main#main-content ul li", new Page.WaitForSelectorOptions().setTimeout(10000));
        } catch (PlaywrightException e) {
            return vagas;
        }

        String html = page.content();
        Document doc = Jsoup.parse(html);

        Elements jobs = doc.select("main#main-content ul li");

        for (Element job : jobs) {
            Element linkElement = job.selectFirst("a");
            String link = (linkElement != null) ? linkElement.attr("href") : "";

            Element tituloElement = job.selectFirst("h3");
            String titulo = (tituloElement != null) ? tituloElement.text() : "Titulo nao encontrado";

            Element empresaElement = job.selectFirst("div[aria-label^=Empresa]");
            String empresa = "Nao informada";
            if (empresaElement != null) {
                empresa = empresaElement.attr("aria-label").replace("Empresa ", "").trim();
            }

            Element localElement = job.selectFirst("span[data-testid='job-location']");
            String localizacao = (localElement != null) ? localElement.text() : "Nao informada";
            if (!local.isBlank() && !localizacao.toLowerCase().contains(local.toLowerCase())) {
                continue;
            }

            if (!titulo.equals("Titulo nao encontrado") && !link.isEmpty()) {
                VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, link, "Gupy");
                vagas.add(vaga);
            }
        }

        return vagas;
    }
}