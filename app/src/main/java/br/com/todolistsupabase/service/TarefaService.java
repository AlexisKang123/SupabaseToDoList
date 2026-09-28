package br.com.todolistsupabase.service;

import android.content.Context;

import java.util.List;

import br.com.todolistsupabase.core.network.ResultadoCallback;
import br.com.todolistsupabase.model.EditarTarefaRequest;
import br.com.todolistsupabase.model.Resultado;
import br.com.todolistsupabase.model.Resultado.Campo;
import br.com.todolistsupabase.model.Tarefa;
import br.com.todolistsupabase.repository.TarefaRepository;

// Regras de negócio: valida os dados e só então chama o repositório.
public class TarefaService {

    private static final int MAX_TITULO = 100;
    private static final int MAX_DESCRICAO = 500;

    private final TarefaRepository tarefaRepository;

    public TarefaService(Context context) {
        tarefaRepository = new TarefaRepository(context);
    }

    public void listar(ResultadoCallback<List<Tarefa>> callback) {
        tarefaRepository.listar(callback);
    }

    public Resultado criar(Tarefa tarefa, ResultadoCallback<Void> callback) {

        if (tarefa == null) {
            return Resultado.erro(Campo.NENHUM, "A tarefa não pode ser nula.");
        }

        Resultado validacao = validarCampos(
                tarefa.getTitulo(), tarefa.getDescricao(), tarefa.getPrioridade());

        if (!validacao.isSucesso()) {
            return validacao;
        }

        tarefa.setConcluida(false); // toda tarefa nova começa pendente
        tarefaRepository.criar(tarefa, callback);
        return Resultado.ok();
    }

    public Resultado editar(String id, EditarTarefaRequest request, ResultadoCallback<Void> callback) {

        Resultado validacao = validarId(id);

        if (validacao.isSucesso()) {
            validacao = request == null
                    ? Resultado.erro(Campo.NENHUM, "Os dados da tarefa são obrigatórios.")
                    : validarCampos(request.getTitulo(), request.getDescricao(), request.getPrioridade());
        }

        if (!validacao.isSucesso()) {
            return validacao;
        }

        tarefaRepository.editar(id, request, callback);
        return Resultado.ok();
    }

    public Resultado alterarConclusao(String id, boolean concluida, ResultadoCallback<Void> callback) {

        Resultado validacao = validarId(id);

        if (validacao.isSucesso()) {
            tarefaRepository.alterarConclusao(id, concluida, callback);
        }

        return validacao;
    }

    public Resultado excluir(String id, ResultadoCallback<Void> callback) {

        Resultado validacao = validarId(id);

        if (validacao.isSucesso()) {
            tarefaRepository.excluir(id, callback);
        }

        return validacao;
    }

    // Uma única validação, usada tanto na criação quanto na edição.
    private Resultado validarCampos(String titulo, String descricao, int prioridade) {

        if (titulo == null || titulo.trim().isEmpty()) {
            return Resultado.erro(Campo.TITULO, "O título da tarefa é obrigatório.");
        }

        if (titulo.trim().length() > MAX_TITULO) {
            return Resultado.erro(Campo.TITULO,
                    "O título pode ter no máximo " + MAX_TITULO + " caracteres.");
        }

        if (descricao != null && descricao.length() > MAX_DESCRICAO) {
            return Resultado.erro(Campo.DESCRICAO,
                    "A descrição pode ter no máximo " + MAX_DESCRICAO + " caracteres.");
        }

        if (prioridade < Tarefa.PRIORIDADE_BAIXA || prioridade > Tarefa.PRIORIDADE_ALTA) {
            return Resultado.erro(Campo.PRIORIDADE, "A prioridade da tarefa é inválida.");
        }

        return Resultado.ok();
    }

    private Resultado validarId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Resultado.erro(Campo.NENHUM, "O ID da tarefa é obrigatório.");
        }
        return Resultado.ok();
    }
}
