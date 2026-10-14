package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;

@Service
public class EnvioEmail {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EnvioEmail(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remetente) {

        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviar(Notificacao notificacao) {

        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(
                notificacao.getDestinatarios().toArray(new String[0])
        );
        mensagem.setSubject(notificacao.getAssunto());
        mensagem.setText(notificacao.getConteudo());

        mailSender.send(mensagem);
    }
}