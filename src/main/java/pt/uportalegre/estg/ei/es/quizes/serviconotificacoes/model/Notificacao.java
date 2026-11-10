package pt.uportalegre.estg.ei.es.quizes.serviconotificacoes.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

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

    @Enumerated(EnumType.STRING)
    private EstadoNotificacao estado = EstadoNotificacao.PENDENTE;

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

    public EstadoNotificacao getEstado() {
        return estado;
    }

    public void setEstado(EstadoNotificacao estado) {
        this.estado = estado;
    }
}
