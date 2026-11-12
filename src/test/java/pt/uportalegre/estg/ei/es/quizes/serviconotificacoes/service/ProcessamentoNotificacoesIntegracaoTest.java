package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.dto.NotificacaoDTO;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email.EnvioEmailSimulado;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email.EnvioNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.NotificacaoRepository;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.TentativaEnvioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"teste", "simulacao"})
@TestPropertySource(locations = "classpath:application-teste.properties")
class ProcessamentoNotificacoesIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicoNotificacoes servicoNotificacoes;

    @Autowired
    private ServicoTentativas servicoTentativas;

    @Autowired
    private ProcessamentoNotificacoes processamentoNotificacoes;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private TentativaEnvioRepository tentativaEnvioRepository;

    @Autowired
    private EnvioNotificacao envioNotificacao;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void preparar() throws SQLException {
        try (var ligacao = dataSource.getConnection()) {
            assertTrue(ligacao.getMetaData().getURL().startsWith("jdbc:h2:mem:"));
        }

        assertInstanceOf(EnvioEmailSimulado.class, envioNotificacao);
        tentativaEnvioRepository.deleteAll();
        notificacaoRepository.deleteAll();
        ReflectionTestUtils.setField(envioNotificacao, "falhasAntesSucesso", 0);
    }

    private Notificacao submeterNotificacao() {
        NotificacaoDTO notificacaoDTO = new NotificacaoDTO(
                List.of("pessoa1@exemplo.pt", "pessoa2@exemplo.pt"),
                "Teste de integração",
                "Mensagem sem envio real."
        );
        return servicoNotificacoes.submeterNotificacao(notificacaoDTO);
    }

    @Test
    void deveIgnorarEstadoEIdEnviadosPeloFormulario() throws Exception {
        mockMvc.perform(post("/notificacao")
                        .param("destinatarios", "pessoa1@exemplo.pt, pessoa2@exemplo.pt")
                        .param("assunto", "Submissão com DTO")
                        .param("conteudo", "Mensagem de teste.")
                        .param("estado", "ENVIADA")
                        .param("id", "-42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notificacao"));

        List<Notificacao> pendentes = notificacaoRepository.findByEstadoIn(
                List.of(EstadoNotificacao.PENDENTE)
        );
        assertEquals(1, notificacaoRepository.count());
        assertEquals(1, pendentes.size());
        Notificacao guardada = pendentes.get(0);
        assertNotNull(guardada.getId());
        assertNotEquals(-42L, guardada.getId());
        assertEquals(EstadoNotificacao.PENDENTE, guardada.getEstado());
        assertEquals("Submissão com DTO", guardada.getAssunto());
        assertEquals("Mensagem de teste.", guardada.getConteudo());
        assertEquals(Set.of("pessoa1@exemplo.pt", "pessoa2@exemplo.pt"),
                Set.copyOf(guardada.getDestinatarios()));
    }

    @Test
    void devePersistirNotificacaoDestinatariosETentativaBemSucedida() {
        Notificacao notificacao = submeterNotificacao();
        assertNotNull(notificacao.getId());
        assertEquals(EstadoNotificacao.PENDENTE, notificacao.getEstado());

        processamentoNotificacoes.processarPendentes();
        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.ENVIADA,
                servicoNotificacoes.consultarNotificacao(notificacao.getId()).getEstado());
        assertEquals(1, servicoTentativas.consultarHistorico(notificacao.getId()).size());
        assertEquals(1, notificacaoRepository.count());
        var destinatariosGuardados =
                notificacaoRepository.findByEstadoIn(List.of(EstadoNotificacao.ENVIADA))
                        .get(0).getDestinatarios();
        assertEquals(2, destinatariosGuardados.size());
        assertEquals(Set.copyOf(notificacao.getDestinatarios()), Set.copyOf(destinatariosGuardados));
    }

    @Test
    void deveGuardarFalhaEConseguirEnviarNaTentativaSeguinte() {
        ReflectionTestUtils.setField(envioNotificacao, "falhasAntesSucesso", 1);
        Notificacao notificacao = submeterNotificacao();

        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.FALHADA,
                servicoNotificacoes.consultarNotificacao(notificacao.getId()).getEstado());
        assertTrue(notificacaoRepository.findById(notificacao.getId()).isPresent());
        assertNotNull(servicoTentativas.consultarHistorico(notificacao.getId()).get(0).getErro());

        processamentoNotificacoes.processarPendentes();

        var historico = servicoTentativas.consultarHistorico(notificacao.getId());
        assertEquals(2, historico.size());
        assertEquals(EstadoNotificacao.FALHADA, historico.get(0).getResultado());
        assertEquals(EstadoNotificacao.ENVIADA, historico.get(1).getResultado());
        assertEquals(EstadoNotificacao.ENVIADA,
                servicoNotificacoes.consultarNotificacao(notificacao.getId()).getEstado());
    }

    @Test
    void devePersistirDuasFalhasConsecutivasAntesDoSucesso() {
        ReflectionTestUtils.setField(envioNotificacao, "falhasAntesSucesso", 2);
        Notificacao notificacao = submeterNotificacao();

        processamentoNotificacoes.processarPendentes();
        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.FALHADA,
                servicoNotificacoes.consultarNotificacao(notificacao.getId()).getEstado());

        processamentoNotificacoes.processarPendentes();

        var historico = servicoTentativas.consultarHistorico(notificacao.getId());
        assertEquals(List.of(EstadoNotificacao.FALHADA, EstadoNotificacao.FALHADA,
                        EstadoNotificacao.ENVIADA),
                historico.stream().map(tentativa -> tentativa.getResultado()).toList());
    }

    @Test
    void deveRespeitarOLimiteSemPerderANotificacao() {
        ReflectionTestUtils.setField(envioNotificacao, "falhasAntesSucesso", 10);
        Notificacao notificacao = submeterNotificacao();

        for (int ciclo = 0; ciclo < 5; ciclo++) {
            processamentoNotificacoes.processarPendentes();
        }

        var historico = servicoTentativas.consultarHistorico(notificacao.getId());
        assertEquals(servicoTentativas.getMaxTentativas(), historico.size());
        assertEquals(List.of(1, 2, 3),
                historico.stream().map(tentativa -> tentativa.getNumero()).toList());
        assertEquals(EstadoNotificacao.FALHADA,
                servicoNotificacoes.consultarNotificacao(notificacao.getId()).getEstado());
        assertTrue(notificacaoRepository.findById(notificacao.getId()).isPresent());
    }
}
