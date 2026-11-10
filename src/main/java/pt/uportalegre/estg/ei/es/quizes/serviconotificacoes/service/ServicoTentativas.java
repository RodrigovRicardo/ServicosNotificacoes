package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.EstadoNotificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.TentativaEnvio;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.NotificacaoRepository;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.repository.TentativaEnvioRepository;

@Service
public class ServicoTentativas {

    @Autowired
    private TentativaEnvioRepository tentativaEnvioRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Value("${notificacao.max-tentativas}")
    private int maxTentativas;

    @PostConstruct
    public void validarConfiguracao() {
        if (maxTentativas < 1) {
            throw new IllegalArgumentException("O limite de tentativas deve ser pelo menos 1.");
        }
    }

    @Transactional
    public TentativaEnvio iniciarTentativa(Notificacao notificacao) {
        if (notificacao.getEstado() == EstadoNotificacao.ENVIADA) {
            return null;
        }

        long realizadas = tentativaEnvioRepository.countByNotificacaoId(notificacao.getId());

        if (realizadas >= maxTentativas) {
            if (notificacao.getEstado() != EstadoNotificacao.FALHADA) {
                notificacao.setEstado(EstadoNotificacao.FALHADA);
                notificacaoRepository.save(notificacao);
            }
            return null;
        }

        TentativaEnvio tentativa = new TentativaEnvio();
        tentativa.setNotificacao(notificacao);
        tentativa.setNumero((int) realizadas + 1);

        return tentativaEnvioRepository.saveAndFlush(tentativa);
    }

    @Transactional
    public void concluirTentativa(
            TentativaEnvio tentativa,
            EstadoNotificacao resultado,
            String erro) {

        tentativa.setResultado(resultado);
        tentativa.setErro(erro);
        tentativaEnvioRepository.save(tentativa);

        Notificacao notificacao = tentativa.getNotificacao();
        notificacao.setEstado(resultado);
        notificacaoRepository.save(notificacao);
    }

    public List<TentativaEnvio> consultarHistorico(Long notificacaoId) {
        return tentativaEnvioRepository.findByNotificacaoIdOrderByNumeroAsc(notificacaoId);
    }

    public int getMaxTentativas() {
        return maxTentativas;
    }
}
