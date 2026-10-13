package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import org.springframework.stereotype.Service;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;

@Service
public class ServicoNotificacoes {

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
}
