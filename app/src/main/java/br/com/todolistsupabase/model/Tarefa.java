package br.com.todolistsupabase.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Serializable para poder ser enviada inteira pela Intent (tela de edição).
public class Tarefa implements Serializable {

    public static final int PRIORIDADE_BAIXA = 1;
    public static final int PRIORIDADE_MEDIA = 2;
    public static final int PRIORIDADE_ALTA = 3;

    private String id;

    // Preenchido pelo banco (default auth.uid()); o app não envia.
    @SerializedName("usuario_id")
    private String usuarioId;

    private String titulo;
    private String descricao;
    private boolean concluida;
    private String data;
    private int prioridade = PRIORIDADE_MEDIA;

    public Tarefa() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }
}
