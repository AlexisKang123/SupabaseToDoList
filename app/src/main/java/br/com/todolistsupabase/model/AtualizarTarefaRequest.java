package br.com.todolistsupabase.model;

// Corpo do PATCH usado pelo checkbox: altera só a conclusão.
public class AtualizarTarefaRequest {

    private final boolean concluida;

    public AtualizarTarefaRequest(boolean concluida) {
        this.concluida = concluida;
    }

    public boolean isConcluida() {
        return concluida;
    }
}
