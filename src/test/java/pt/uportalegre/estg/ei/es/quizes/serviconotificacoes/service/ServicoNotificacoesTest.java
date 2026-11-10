package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.NotificacaoRepository;

@ExtendWith(MockitoExtension.class)
class ServicoNotificacoesTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @InjectMocks
    private ServicoNotificacoes servicoNotificacoes;

    @Test
    void deveGuardarSubmissaoComoPendente() {
        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatarios(List.of("pessoa@exemplo.pt"));
        notificacao.setAssunto("Teste");
        notificacao.setConteudo("Mensagem de teste.");
        notificacao.setEstado(EstadoNotificacao.ENVIADA);

        when(notificacaoRepository.save(notificacao)).thenReturn(notificacao);

        Notificacao resultado = servicoNotificacoes.submeterNotificacao(notificacao);

        assertSame(notificacao, resultado);
        assertEquals(EstadoNotificacao.PENDENTE, resultado.getEstado());
        verify(notificacaoRepository).save(notificacao);
    }

    @Test
    void deveInicializarRegistosAntigosComoEnviados() {
        Notificacao notificacaoAntiga = new Notificacao();
        notificacaoAntiga.setEstado(null);
        List<Notificacao> notificacoesAntigas = List.of(notificacaoAntiga);

        when(notificacaoRepository.findByEstadoIsNull())
                .thenReturn(notificacoesAntigas);

        servicoNotificacoes.inicializarEstadosExistentes();

        assertEquals(EstadoNotificacao.ENVIADA, notificacaoAntiga.getEstado());
        verify(notificacaoRepository).saveAll(notificacoesAntigas);
    }
}
