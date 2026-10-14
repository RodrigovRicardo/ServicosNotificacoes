package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {
}
