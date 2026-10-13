package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model;

import java.util.ArrayList;
import java.util.List;

public class Notificacao {

    private List<String> destinatarios = new ArrayList<>();
    private String assunto;
    private String conteudo;
    public List<String> getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(List<String> destinatarios) {
        this.destinatarios = destinatarios;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

}
