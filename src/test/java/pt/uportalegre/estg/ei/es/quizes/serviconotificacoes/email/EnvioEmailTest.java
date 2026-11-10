package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;

class EnvioEmailTest {

    @Test
    void deveConstruirUmaMensagemComTodosOsDestinatarios() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EnvioEmail envioEmail = new EnvioEmail(mailSender, "remetente@exemplo.pt");
        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatarios(List.of("pessoa1@exemplo.pt", "pessoa2@exemplo.pt"));
        notificacao.setAssunto("Reunião");
        notificacao.setConteudo("A reunião será amanhã.");

        envioEmail.enviar(notificacao);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage mensagem = captor.getValue();
        assertEquals("remetente@exemplo.pt", mensagem.getFrom());
        assertArrayEquals(
                new String[]{"pessoa1@exemplo.pt", "pessoa2@exemplo.pt"}, mensagem.getTo());
        assertEquals("Reunião", mensagem.getSubject());
        assertEquals("A reunião será amanhã.", mensagem.getText());
    }
}
