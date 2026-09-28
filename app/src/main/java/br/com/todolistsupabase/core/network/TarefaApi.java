package br.com.todolistsupabase.core.network;

import java.util.List;

import br.com.todolistsupabase.model.AtualizarTarefaRequest;
import br.com.todolistsupabase.model.EditarTarefaRequest;
import br.com.todolistsupabase.model.Tarefa;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

// Os headers "apikey" e "Authorization" são adicionados pelos interceptors.
// Os filtros seguem a sintaxe do PostgREST: id=eq.<uuid>, order=coluna.asc etc.
public interface TarefaApi {

    @GET("rest/v1/tarefas")
    Call<List<Tarefa>> listarTarefas(
            @Query("select") String select,
            @Query("order") String order
    );

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/tarefas")
    Call<Void> criarTarefa(@Body Tarefa tarefa);

    @PATCH("rest/v1/tarefas")
    Call<Void> alterarConclusao(
            @Query("id") String filtroId,
            @Body AtualizarTarefaRequest request
    );

    @PATCH("rest/v1/tarefas")
    Call<Void> editarTarefa(
            @Query("id") String filtroId,
            @Body EditarTarefaRequest request
    );

    @DELETE("rest/v1/tarefas")
    Call<Void> excluirTarefa(@Query("id") String filtroId);
}
