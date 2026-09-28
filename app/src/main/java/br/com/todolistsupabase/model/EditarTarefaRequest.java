package br.com.todolistsupabase.model;

// Corpo do PATCH da tela de edição.
// Não inclui "concluida": a edição não mexe no status, que é controlado pelo checkbox.
public class EditarTarefaRequest {

    private final String titulo;
    private final String descricao;
    private final String data;
    private final int prioridade;

    public EditarTarefaRequest(String titulo, String descricao, String data, int prioridade) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.prioridade = prioridade;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getData() {
        return data;
    }

    public int getPrioridade() {
        return prioridade;
    }
}
