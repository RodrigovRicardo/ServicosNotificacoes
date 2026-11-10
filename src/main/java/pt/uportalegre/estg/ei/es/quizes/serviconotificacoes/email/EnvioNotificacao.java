package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.email;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;

public interface EnvioNotificacao {

    void enviar(Notificacao notificacao);
}
