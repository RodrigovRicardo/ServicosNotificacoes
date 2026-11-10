package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email.EnvioEmail;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.TentativaEnvio;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.NotificacaoRepository;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.TentativaEnvioRepository;

@ExtendWith(MockitoExtension.class)
class ProcessamentoNotificacoesTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private TentativaEnvioRepository tentativaEnvioRepository;

    @Mock
    private EnvioEmail envioEmail;

    @InjectMocks
    private ServicoTentativas servicoTentativas;

    private ProcessamentoNotificacoes processamentoNotificacoes;
    private Notificacao notificacao;
    private List<TentativaEnvio> historico;

    @BeforeEach
    void preparar() {
        notificacao = new Notificacao();
        notificacao.setDestinatarios(List.of("pessoa@exemplo.pt"));
        notificacao.setAssunto("Teste");
        notificacao.setConteudo("Mensagem de teste.");
        historico = new ArrayList<>();

        ReflectionTestUtils.setField(notificacao, "id", 1L);
        ReflectionTestUtils.setField(servicoTentativas, "maxTentativas", 3);

        processamentoNotificacoes = new ProcessamentoNotificacoes();
        ReflectionTestUtils.setField(
                processamentoNotificacoes, "notificacaoRepository", notificacaoRepository);
        ReflectionTestUtils.setField(
                processamentoNotificacoes, "envioNotificacao", envioEmail);
        ReflectionTestUtils.setField(
                processamentoNotificacoes, "servicoTentativas", servicoTentativas);

        when(notificacaoRepository.findByEstadoIn(anyList()))
                .thenAnswer(invocacao -> {
                    List<EstadoNotificacao> estados = invocacao.getArgument(0);
                    return estados.contains(notificacao.getEstado())
                            ? List.of(notificacao) : List.of();
                });

        when(tentativaEnvioRepository.countByNotificacaoId(1L))
                .thenAnswer(invocacao -> (long) historico.size());

        when(tentativaEnvioRepository.saveAndFlush(any(TentativaEnvio.class)))
                .thenAnswer(invocacao -> {
                    TentativaEnvio tentativa = invocacao.getArgument(0);
                    historico.add(tentativa);
                    return tentativa;
                });
    }

    @Test
    void deveRegistarSucessoSemVoltarAEnviar() {
        processamentoNotificacoes.processarPendentes();
        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.ENVIADA, notificacao.getEstado());
        assertEquals(1, historico.size());
        assertEquals(1, historico.get(0).getNumero());
        assertEquals(EstadoNotificacao.ENVIADA, historico.get(0).getResultado());
        assertNotNull(historico.get(0).getDataHora());
        assertNull(historico.get(0).getErro());
        verify(envioEmail, times(1)).enviar(notificacao);
        verify(tentativaEnvioRepository).save(historico.get(0));
    }

    @Test
    void deveRegistarUmaFalhaSeguidaDeSucesso() {
        doThrow(new MailSendException("Falha temporária."))
                .doNothing()
                .when(envioEmail).enviar(notificacao);

        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.FALHADA, notificacao.getEstado());
        assertEquals("Falha temporária.", historico.get(0).getErro());

        processamentoNotificacoes.processarPendentes();
        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.ENVIADA, notificacao.getEstado());
        assertEquals(2, historico.size());
        assertEquals(EstadoNotificacao.FALHADA, historico.get(0).getResultado());
        assertEquals(EstadoNotificacao.ENVIADA, historico.get(1).getResultado());
        verify(envioEmail, times(2)).enviar(notificacao);
    }

    @Test
    void devePermitirSucessoDepoisDeFalhasConsecutivas() {
        doThrow(new MailSendException("Primeira falha."))
                .doThrow(new MailSendException("Segunda falha."))
                .doNothing()
                .when(envioEmail).enviar(notificacao);

        processamentoNotificacoes.processarPendentes();
        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.FALHADA, notificacao.getEstado());
        assertEquals(2, historico.size());

        processamentoNotificacoes.processarPendentes();

        assertEquals(EstadoNotificacao.ENVIADA, notificacao.getEstado());
        assertEquals(3, historico.size());
        assertEquals("Primeira falha.", historico.get(0).getErro());
        assertEquals("Segunda falha.", historico.get(1).getErro());
        verify(envioEmail, times(3)).enviar(notificacao);
    }

    @Test
    void devePararQuandoAtingeOLimiteDeTentativas() {
        doThrow(new MailSendException("Serviço indisponível."))
                .when(envioEmail).enviar(notificacao);

        for (int ciclo = 0; ciclo < 5; ciclo++) {
            processamentoNotificacoes.processarPendentes();
        }

        assertEquals(EstadoNotificacao.FALHADA, notificacao.getEstado());
        assertEquals(3, historico.size());

        for (int indice = 0; indice < historico.size(); indice++) {
            assertEquals(indice + 1, historico.get(indice).getNumero());
            assertEquals(EstadoNotificacao.FALHADA, historico.get(indice).getResultado());
        }

        verify(envioEmail, times(3)).enviar(notificacao);
    }

    @Test
    void deveContarUmaTentativaInterrompidaAntesDeTentarNovamente() {
        TentativaEnvio interrompida = new TentativaEnvio();
        interrompida.setNotificacao(notificacao);
        interrompida.setNumero(1);
        historico.add(interrompida);

        processamentoNotificacoes.processarPendentes();

        assertEquals(2, historico.size());
        assertEquals(EstadoNotificacao.PENDENTE, interrompida.getResultado());
        assertEquals(2, historico.get(1).getNumero());
        assertEquals(EstadoNotificacao.ENVIADA, notificacao.getEstado());
        verify(envioEmail, times(1)).enviar(notificacao);
    }
}
