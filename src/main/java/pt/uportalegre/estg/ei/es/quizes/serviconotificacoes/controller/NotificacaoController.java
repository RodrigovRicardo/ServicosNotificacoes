package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.dto.NotificacaoDTO;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service.ServicoNotificacoes;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service.ServicoTentativas;

@Controller
public class NotificacaoController {

    @Autowired
    private ServicoNotificacoes servicoNotificacoes;

    @Autowired
    private ServicoTentativas servicoTentativas;

    @GetMapping({"/", "/notificacao/nova"})
    public String mostrarFormulario() {
        return "notificacao";
    }

    @PostMapping("/notificacao")
    public String submeterFormulario(
            @ModelAttribute NotificacaoDTO notificacaoDTO,
            Model model) {

        try {
            servicoNotificacoes.submeterNotificacao(notificacaoDTO);

            return "redirect:/notificacao";

        } catch (IllegalArgumentException erro) {
            model.addAttribute("mensagem", erro.getMessage());

        }

        return "notificacao";
    }

    @GetMapping("/notificacao")
    public String listarNotificacoes(Model model) {

        model.addAttribute(
                "notificacoes",
                servicoNotificacoes.listarNotificacoes()
        );

        return "lista-notificacoes";
    }

    @GetMapping("/notificacao/{id}")
    public String consultarNotificacao(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute(
                    "notificacao",
                    servicoNotificacoes.consultarNotificacao(id)
            );
            model.addAttribute("tentativas", servicoTentativas.consultarHistorico(id));
            model.addAttribute("maxTentativas", servicoTentativas.getMaxTentativas());
        } catch (IllegalArgumentException erro) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    erro.getMessage()
            );
        }

        return "detalhes-notificacao";
    }
}
