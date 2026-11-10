package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "notificacao.processamento.ativo",
        havingValue = "true",
        matchIfMissing = true
)
public class ConfiguracaoAgendamento {
}
