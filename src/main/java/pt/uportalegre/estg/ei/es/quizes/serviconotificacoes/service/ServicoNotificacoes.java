package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import org.springframework.stereotype.Service;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email.EnvioEmail;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.NotificacaoRepository;
import java.util.List;

@Service
public class ServicoNotificacoes {

    private final NotificacaoRepository notificacaoRepository;
    private final EnvioEmail envioEmail;

    public ServicoNotificacoes(
            NotificacaoRepository notificacaoRepository,
            EnvioEmail envioEmail) {

        this.notificacaoRepository = notificacaoRepository;
        this.envioEmail = envioEmail;
    }

    public Notificacao submeterNotificacao(Notificacao notificacao) {

        validarNotificacao(notificacao);

        envioEmail.enviar(notificacao);

        return notificacaoRepository.save(notificacao);
    }

    public void validarNotificacao(Notificacao notificacao) {

        if (notificacao.getDestinatarios() == null
                || notificacao.getDestinatarios().isEmpty()) {
            throw new IllegalArgumentException(
                    "Indica pelo menos um destinatário."
            );
        }

        for (String destinatario : notificacao.getDestinatarios()) {
            validarCampoObrigatorio(destinatario, "Destinatário");
        }

        validarCampoObrigatorio(notificacao.getAssunto(), "Assunto");
        validarCampoObrigatorio(notificacao.getConteudo(), "Conteúdo");
    }

    private void validarCampoObrigatorio(String valor, String campo) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " é obrigatório."
            );
        }
    }
    public List<Notificacao> listarNotificacoes() {
        return notificacaoRepository.findAll();
    }

    public Notificacao consultarNotificacao(Long id) {
        return notificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notificação não encontrada.")
                );
    }
}