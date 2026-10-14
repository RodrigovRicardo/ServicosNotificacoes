package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model.Notificacao;
import pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.service.ServicoNotificacoes;

import org.springframework.mail.MailException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class NotificacaoController {

    private final ServicoNotificacoes servicoNotificacoes;

    public NotificacaoController(ServicoNotificacoes servicoNotificacoes) {
        this.servicoNotificacoes = servicoNotificacoes;
    }

    @GetMapping({"/", "/notificacoes/nova"})
    public String mostrarFormulario() {
        return "notificacao";
    }

    @PostMapping("/notificacoes")
    public String receberFormulario(
            @ModelAttribute Notificacao notificacao,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            servicoNotificacoes.submeterNotificacao(notificacao);

            redirectAttributes.addFlashAttribute(
                    "mensagem",
                    "Notificação enviada e registada."
            );

            return "redirect:/notificacoes";

        } catch (IllegalArgumentException erro) {
            model.addAttribute("mensagem", erro.getMessage());

        } catch (MailException erro) {
            model.addAttribute(
                    "mensagem",
                    "Não foi possível enviar o email."
            );
        }

        return "notificacao";
    }

    @GetMapping("/notificacoes")
    public String listarNotificacoes(Model model) {

        model.addAttribute(
                "notificacoes",
                servicoNotificacoes.listarNotificacoes()
        );

        return "lista-notificacoes";
    }

    @GetMapping("/notificacoes/{id}")
    public String consultarNotificacao(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute(
                    "notificacao",
                    servicoNotificacoes.consultarNotificacao(id)
            );
        } catch (IllegalArgumentException erro) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    erro.getMessage()
            );
        }

        return "detalhes-notificacao";
    }
}