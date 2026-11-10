package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"teste", "simulacao"})
@TestPropertySource(locations = "classpath:application-teste.properties")
class ServicoNotificacoesApplicationTests {

    @Test
    void contextLoads() {
    }
}
