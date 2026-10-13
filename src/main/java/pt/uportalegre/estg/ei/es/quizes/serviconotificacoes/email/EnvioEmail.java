package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;

@Service
public class EnvioEmail {

    private final JavaMailSender mailSender;

    public EnvioEmail(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviar(Notificacao notificacao) {

        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(
                notificacao.getDestinatarios().toArray(new String[0])
        );
        mensagem.setSubject(notificacao.getAssunto());
        mensagem.setText(notificacao.getConteudo());

        mailSender.send(mensagem);
    }
}