package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.TentativaEnvio;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service.ServicoNotificacoes;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service.ServicoTentativas;

@WebMvcTest(NotificacaoController.class)
@ActiveProfiles("teste")
@TestPropertySource(locations = "classpath:application-teste.properties")
class NotificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicoNotificacoes servicoNotificacoes;

    @MockitoBean
    private ServicoTentativas servicoTentativas;

    private Notificacao notificacao;

    @BeforeEach
    void preparar() {
        notificacao = new Notificacao();
        ReflectionTestUtils.setField(notificacao, "id", 7L);
        notificacao.setDestinatarios(List.of("pessoa@exemplo.pt"));
        notificacao.setAssunto("Teste Web");
        notificacao.setConteudo("Conteúdo do teste.");
        notificacao.setEstado(EstadoNotificacao.ENVIADA);
    }

    @Test
    void deveApresentarFormularioNaPaginaInicial() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("notificacao"))
                .andExpect(content().string(containsString("/css/estilos.css")));
    }

    @Test
    void deveApresentarFormularioNoEnderecoPadronizado() throws Exception {
        mockMvc.perform(get("/notificacao/nova"))
                .andExpect(status().isOk())
                .andExpect(view().name("notificacao"));
    }

    @Test
    void deveListarNotificacoes() throws Exception {
        when(servicoNotificacoes.listarNotificacoes()).thenReturn(List.of(notificacao));

        mockMvc.perform(get("/notificacao"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-notificacoes"))
                .andExpect(model().attribute("notificacoes", List.of(notificacao)))
                .andExpect(content().string(containsString("/notificacao/7")));
    }

    @Test
    void deveMostrarMensagemQuandoListaEstaVazia() throws Exception {
        when(servicoNotificacoes.listarNotificacoes()).thenReturn(List.of());

        mockMvc.perform(get("/notificacao"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Não existem notificações registadas.")));
    }

    @Test
    void deveApresentarDetalhesComHistorico() throws Exception {
        TentativaEnvio tentativa = new TentativaEnvio();
        tentativa.setNotificacao(notificacao);
        tentativa.setNumero(1);
        tentativa.setResultado(EstadoNotificacao.ENVIADA);

        when(servicoNotificacoes.consultarNotificacao(7L)).thenReturn(notificacao);
        when(servicoTentativas.consultarHistorico(7L)).thenReturn(List.of(tentativa));
        when(servicoTentativas.getMaxTentativas()).thenReturn(3);

        mockMvc.perform(get("/notificacao/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalhes-notificacao"))
                .andExpect(model().attribute("tentativas", List.of(tentativa)))
                .andExpect(content().string(containsString("Histórico de tentativas")));
    }

    @Test
    void deveDevolver404ParaNotificacaoInexistente() throws Exception {
        when(servicoNotificacoes.consultarNotificacao(99L))
                .thenThrow(new IllegalArgumentException("Notificação não encontrada."));

        mockMvc.perform(get("/notificacao/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveSubmeterVariosDestinatariosERedirecionar() throws Exception {
        mockMvc.perform(post("/notificacao")
                        .param("destinatarios", "pessoa1@exemplo.pt, pessoa2@exemplo.pt")
                        .param("assunto", "Teste Web")
                        .param("conteudo", "Mensagem de teste."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notificacao"));

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(servicoNotificacoes).submeterNotificacao(captor.capture());
        assertEquals(
                List.of("pessoa1@exemplo.pt", "pessoa2@exemplo.pt"),
                captor.getValue().getDestinatarios());
    }

    @Test
    void deveMostrarErroDoServicoNoFormulario() throws Exception {
        doThrow(new IllegalArgumentException("Assunto é obrigatório."))
                .when(servicoNotificacoes).submeterNotificacao(any(Notificacao.class));

        mockMvc.perform(post("/notificacao")
                        .param("destinatarios", "pessoa@exemplo.pt")
                        .param("conteudo", "Mensagem de teste."))
                .andExpect(status().isOk())
                .andExpect(view().name("notificacao"))
                .andExpect(model().attribute("mensagem", "Assunto é obrigatório."));
    }
}
