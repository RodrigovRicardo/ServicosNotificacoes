package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.TentativaEnvioRepository;

@ExtendWith(MockitoExtension.class)
class EnvioEmailSimuladoTest {

    @Mock
    private TentativaEnvioRepository tentativaEnvioRepository;

    @InjectMocks
    private EnvioEmailSimulado envioEmailSimulado;

    private Notificacao notificacao;

    @BeforeEach
    void preparar() {
        notificacao = new Notificacao();
        ReflectionTestUtils.setField(notificacao, "id", 1L);
    }

    @Test
    void deveSimularUmaFalhaSeguidaDeSucesso() {
        ReflectionTestUtils.setField(envioEmailSimulado, "falhasAntesSucesso", 1);
        when(tentativaEnvioRepository.countByNotificacaoId(1L)).thenReturn(1L, 2L);

        assertThrows(MailSendException.class, () -> envioEmailSimulado.enviar(notificacao));
        assertDoesNotThrow(() -> envioEmailSimulado.enviar(notificacao));
    }

    @Test
    void devePermitirSucessoSemFalhasSimuladas() {
        ReflectionTestUtils.setField(envioEmailSimulado, "falhasAntesSucesso", 0);
        when(tentativaEnvioRepository.countByNotificacaoId(1L)).thenReturn(1L);

        assertDoesNotThrow(() -> envioEmailSimulado.enviar(notificacao));
    }
}
