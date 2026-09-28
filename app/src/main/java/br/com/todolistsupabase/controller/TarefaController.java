package br.com.todolistsupabase.controller;

import android.content.Context;

import java.util.List;

import br.com.todolistsupabase.core.network.ResultadoCallback;
import br.com.todolistsupabase.core.util.DataUtils;
import br.com.todolistsupabase.model.EditarTarefaRequest;
import br.com.todolistsupabase.model.Resultado;
import br.com.todolistsupabase.model.Resultado.Campo;
import br.com.todolistsupabase.model.Tarefa;
import br.com.todolistsupabase.service.TarefaService;

/**
 * Ponte entre a tela e as regras de negócio.
 * Recebe os valores como estão na tela (texto, data dd/MM/yyyy),
 * converte para o formato do model e chama o service.
 */
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(Context context) {
        tarefaService = new TarefaService(context);
    }

    public void listar(ResultadoCallback<List<Tarefa>> callback) {
        tarefaService.listar(callback);
    }

    public Resultado criar(String titulo, String descricao, String dataTela,
                           int prioridade, ResultadoCallback<Void> callback) {

        String data = DataUtils.paraSupabase(dataTela);

        if (dataInvalida(dataTela, data)) {
            return Resultado.erro(Campo.DATA, "Data inválida.");
        }

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(limpar(titulo));
        tarefa.setDescricao(limpar(descricao));
        tarefa.setData(data);
        tarefa.setPrioridade(prioridade);

        return tarefaService.criar(tarefa, callback);
    }

    public Resultado editar(String id, String titulo, String descricao, String dataTela,
                            int prioridade, ResultadoCallback<Void> callback) {

        String data = DataUtils.paraSupabase(dataTela);

        if (dataInvalida(dataTela, data)) {
            return Resultado.erro(Campo.DATA, "Data inválida.");
        }

        EditarTarefaRequest request = new EditarTarefaRequest(
                limpar(titulo), limpar(descricao), data, prioridade);

        return tarefaService.editar(id, request, callback);
    }

    public Resultado alterarConclusao(Tarefa tarefa, boolean concluida, ResultadoCallback<Void> callback) {
        return tarefaService.alterarConclusao(tarefa.getId(), concluida, callback);
    }

    public Resultado excluir(Tarefa tarefa, ResultadoCallback<Void> callback) {
        return tarefaService.excluir(tarefa.getId(), callback);
    }

    private boolean dataInvalida(String dataTela, String dataConvertida) {
        return dataTela != null && !dataTela.trim().isEmpty() && dataConvertida == null;
    }

    private String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
