package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notificacoes")

public class Notificacao{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(
            name = "destinatarios_notificacao",
            joinColumns = @JoinColumn(name = "notificacao_id")
    )

    @Column(name = "destinatario")
    private List<String> destinatarios = new ArrayList<>();
    @Column(columnDefinition = "TEXT")
    private String assunto;
    @Column(columnDefinition = "TEXT")
    private String conteudo;
    public Long getId() {
        return id;
    }
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
