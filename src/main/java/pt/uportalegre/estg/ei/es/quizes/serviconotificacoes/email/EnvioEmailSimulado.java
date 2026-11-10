package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.TentativaEnvioRepository;

@Service
@Profile("simulacao")
public class EnvioEmailSimulado implements EnvioNotificacao {

    @Autowired
    private TentativaEnvioRepository tentativaEnvioRepository;

    @Value("${notificacao.simulacao.falhas-antes-sucesso:0}")
    private int falhasAntesSucesso;

    @PostConstruct
    public void validarConfiguracao() {
        if (falhasAntesSucesso < 0) {
            throw new IllegalArgumentException("O número de falhas simuladas não pode ser negativo.");
        }
    }

    @Override
    public void enviar(Notificacao notificacao) {
        long numeroTentativa =
                tentativaEnvioRepository.countByNotificacaoId(notificacao.getId());

        if (falhasAntesSucesso > 0 && numeroTentativa <= falhasAntesSucesso) {
            throw new MailSendException("Falha simulada na tentativa " + numeroTentativa + ".");
        }
    }
}
