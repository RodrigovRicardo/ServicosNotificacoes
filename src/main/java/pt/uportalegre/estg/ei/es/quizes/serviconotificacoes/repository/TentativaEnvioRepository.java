package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.TentativaEnvio;

public interface TentativaEnvioRepository extends JpaRepository<TentativaEnvio, Long> {

    long countByNotificacaoId(Long notificacaoId);

    List<TentativaEnvio> findByNotificacaoIdOrderByNumeroAsc(Long notificacaoId);
}
