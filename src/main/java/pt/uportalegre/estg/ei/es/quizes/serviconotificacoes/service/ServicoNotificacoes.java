package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.NotificacaoRepository;

@Service
public class ServicoNotificacoes {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @PostConstruct
    public void inicializarEstadosExistentes() {
        List<Notificacao> notificacoes = notificacaoRepository.findByEstadoIsNull();

        for (Notificacao notificacao : notificacoes) {
            notificacao.setEstado(EstadoNotificacao.ENVIADA);
        }

        if (!notificacoes.isEmpty()) {
            notificacaoRepository.saveAll(notificacoes);
        }
    }

    public Notificacao submeterNotificacao(Notificacao notificacao) {
        notificacao.setEstado(EstadoNotificacao.PENDENTE);

        return notificacaoRepository.save(notificacao);
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
