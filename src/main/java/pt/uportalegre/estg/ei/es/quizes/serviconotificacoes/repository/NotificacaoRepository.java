package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByEstadoIsNull();

    @EntityGraph(attributePaths = "destinatarios")
    List<Notificacao> findByEstadoIn(List<EstadoNotificacao> estados);
}
