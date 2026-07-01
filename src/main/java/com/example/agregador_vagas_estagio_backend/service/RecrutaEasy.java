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

            // 1. Inicia o browser em modo headless (invisível)
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            // 2. Navega até a página
            page.navigate(url);

            // Espera a rede acalmar (como você já colocou)
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // 3. ADICIONE O PAUSE AQUI! 
            // O código vai parar nessa linha. O navegador vai ficar aberto na sua tela.
            // Quando você terminar de olhar, é só ir no Playwright Inspector e clicar no botão de "Resume" (Play)
            // page.pause();

            // 4. Continua o script normalmente depois que você liberar...
            String htmlRenderizado = page.content();
            browser.close();

            // 5. Passa o HTML completo (já com os cards) para o Jsoup fazer o parse
            Document doc = Jsoup.parse(htmlRenderizado);

            // Pega todos os cards de vagas
            Elements jobs = doc.select("article.relative.bg-white.shadow-xl");

            // System.out.println(jobs);


            for (Element job : jobs) {

                System.out.println(job);
                
                Element titleElement = job.selectFirst("h1"); 
                String titulo = (titleElement != null) ? titleElement.text() : "Não encontrado";
                
                Element employerElement = job.selectFirst("h1 + p"); 
                String empresa = (employerElement != null) ? employerElement.text() : "Não encontrado";
                
                Elements infoSpans = job.select("span[class='flex items-center gap-1.5']");

                String localizacao = "Não encontrado";

                if (!infoSpans.isEmpty()) {
                    localizacao = infoSpans.first().text(); 
                }

                System.out.println("Localização extraída: " + localizacao);
                Element linkElement = job.selectFirst("a[href]"); 
                String linkVaga = (linkElement != null) ? linkElement.attr("href") : "";
                
                if (linkVaga.startsWith("/")) {
                    linkVaga = "https://recrutaeasy.com.br" + linkVaga;
                }

                // Filtro de localização (Ignora case e aplica filtro se foi passado pelo usuário)
                // if (!local.isBlank() && !localizacao.equals("Não encontrado")) {
                //     if (!localizacao.toLowerCase().contains(local.toLowerCase())) {
                //         continue;
                //     }
                // }

                // Element locationElement = job.selectFirst("div.flex-wrap > span.flex.items-center.gap-1.5");

                // String localizacao = (locationElement != null) ? locationElement.text() : "Não encontrado";

                VagaDTO vaga = new VagaDTO(titulo, empresa, localizacao, linkVaga, "RecrutaEasy");
                
                listaDeVagas.add(vaga);
            }

            return listaDeVagas;

        } catch (Exception e) { 
            // Troquei para "Exception" genérica, assim capturamos tanto o PlaywrightException (ex: timeout)
            // quanto qualquer outro erro na leitura, evitando que o backend caia.
            System.err.println("Erro ao realizar scraping com Playwright: " + e.getMessage());
            return listaDeVagas; 
        }
    }
}