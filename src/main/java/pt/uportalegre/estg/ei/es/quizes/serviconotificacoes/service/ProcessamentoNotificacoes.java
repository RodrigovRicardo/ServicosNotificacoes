package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email.EnvioNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.TentativaEnvio;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.NotificacaoRepository;

@Service
public class ProcessamentoNotificacoes {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private EnvioNotificacao envioNotificacao;

    @Autowired
    private ServicoTentativas servicoTentativas;

    @Scheduled(fixedDelay = 5000)
    public void processarPendentes() {
        List<Notificacao> notificacoes =
                notificacaoRepository.findByEstadoIn(
                        List.of(EstadoNotificacao.PENDENTE, EstadoNotificacao.FALHADA)
                );

        for (Notificacao notificacao : notificacoes) {
            TentativaEnvio tentativa = servicoTentativas.iniciarTentativa(notificacao);

            if (tentativa == null) {
                continue;
            }

            EstadoNotificacao resultado;
            String mensagemErro = null;

            try {
                envioNotificacao.enviar(notificacao);
                resultado = EstadoNotificacao.ENVIADA;
            } catch (MailException erro) {
                resultado = EstadoNotificacao.FALHADA;
                mensagemErro = erro.getMessage();
            }

            servicoTentativas.concluirTentativa(tentativa, resultado, mensagemErro);
        }
    }
}
