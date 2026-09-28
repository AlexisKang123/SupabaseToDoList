package br.com.todolistsupabase.repository;

import android.content.Context;

import java.util.List;

import br.com.todolistsupabase.core.network.RespostaCallback;
import br.com.todolistsupabase.core.network.ResultadoCallback;
import br.com.todolistsupabase.core.network.RetrofitClient;
import br.com.todolistsupabase.core.network.TarefaApi;
import br.com.todolistsupabase.model.AtualizarTarefaRequest;
import br.com.todolistsupabase.model.EditarTarefaRequest;
import br.com.todolistsupabase.model.Tarefa;

public class TarefaRepository {

    // Mais próximas primeiro, sem data no fim; empate: maior prioridade primeiro.
    private static final String ORDEM = "data.asc.nullslast,prioridade.desc";

    private final TarefaApi tarefaApi;

    public TarefaRepository(Context context) {
        tarefaApi = RetrofitClient.getInstance(context).create(TarefaApi.class);
    }

    public void listar(ResultadoCallback<List<Tarefa>> callback) {
        tarefaApi.listarTarefas("*", ORDEM).enqueue(new RespostaCallback<>(callback));
    }

    public void criar(Tarefa tarefa, ResultadoCallback<Void> callback) {
        tarefaApi.criarTarefa(tarefa).enqueue(new RespostaCallback<>(callback));
    }

    public void alterarConclusao(String id, boolean concluida, ResultadoCallback<Void> callback) {
        tarefaApi.alterarConclusao(filtroId(id), new AtualizarTarefaRequest(concluida))
                .enqueue(new RespostaCallback<>(callback));
    }

    public void editar(String id, EditarTarefaRequest request, ResultadoCallback<Void> callback) {
        tarefaApi.editarTarefa(filtroId(id), request).enqueue(new RespostaCallback<>(callback));
    }

    public void excluir(String id, ResultadoCallback<Void> callback) {
        tarefaApi.excluirTarefa(filtroId(id)).enqueue(new RespostaCallback<>(callback));
    }

    private String filtroId(String id) {
        return "eq." + id;
    }
}
