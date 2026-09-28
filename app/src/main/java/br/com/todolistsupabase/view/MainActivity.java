package br.com.todolistsupabase.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import br.com.todolistsupabase.R;
import br.com.todolistsupabase.adapter.TarefaAdapter;
import br.com.todolistsupabase.controller.TarefaController;
import br.com.todolistsupabase.controller.UsuarioController;
import br.com.todolistsupabase.core.util.DataUtils;
import br.com.todolistsupabase.model.Resultado;
import br.com.todolistsupabase.model.Tarefa;

public class MainActivity extends BaseActivity {

    private TextView txtSair, txtProgresso, txtDataHoje, txtNome, txtSemTarefas;
    private RecyclerView recycler;
    private FloatingActionButton btnAdd;
    private ProgressBar progressBar;
    private FrameLayout loadingOverlay;

    private final List<Tarefa> tarefas = new ArrayList<>();
    private TarefaAdapter tarefaAdapter;

    private TarefaController tarefaController;
    private UsuarioController usuarioController;

    // O overlay de carregamento só aparece na primeira abertura da tela.
    private boolean usuarioCarregado = false;
    private boolean tarefasCarregadas = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tarefaController = new TarefaController(this);
        usuarioController = new UsuarioController(this);

        inicializarComponentes();
        configurarLista();
        configurarEventos();

        txtDataHoje.setText(DataUtils.hojePorExtenso());

        mostrarLoading();
        carregarUsuario();
        // As tarefas são carregadas no onResume, que sempre roda logo após o onCreate.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Também recarrega ao voltar da tela de criar/editar tarefa.
        listarTarefas();
    }

    private void inicializarComponentes() {
        txtSair = findViewById(R.id.txtSair);
        btnAdd = findViewById(R.id.btnAdd);
        txtProgresso = findViewById(R.id.txtProgresso);
        progressBar = findViewById(R.id.progressBar);
        txtDataHoje = findViewById(R.id.txtDataHoje);
        txtNome = findViewById(R.id.txtNome);
        txtSemTarefas = findViewById(R.id.txtSemTarefas);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        recycler = findViewById(R.id.recycler);
    }

    private void configurarLista() {

        tarefaAdapter = new TarefaAdapter(
                tarefas,
                this::alterarConclusao,
                this::mostrarConfirmacaoExclusao,
                this::abrirEdicaoTarefa
        );

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(tarefaAdapter);
    }

    private void configurarEventos() {

        txtSair.setOnClickListener(view -> mostrarConfirmacaoSaida());

        btnAdd.setOnClickListener(view ->
                startActivity(new Intent(this, NovaTarefaActivity.class)));
    }

    // ---------- Carregamento ----------

    private void carregarUsuario() {

        usuarioController.obterNome(new TelaCallback<String>() {

            @Override
            public void onSucesso(String nome) {
                if (nome != null && !nome.trim().isEmpty()) {
                    txtNome.setText("Oi, " + nome + ".");
                }
                marcarUsuarioCarregado();
            }

            @Override
            public void onErro(String mensagem) {
                // Sem o nome a tela continua funcionando; não mostra erro.
                marcarUsuarioCarregado();
            }
        });
    }

    private void listarTarefas() {

        tarefaController.listar(new TelaCallback<List<Tarefa>>() {

            @Override
            public void onSucesso(List<Tarefa> recebidas) {

                tarefas.clear();

                if (recebidas != null) {
                    tarefas.addAll(recebidas);
                }

                tarefaAdapter.notifyDataSetChanged(); // lista inteira foi trocada
                atualizarProgresso();
                atualizarEstadoVazio();
                marcarTarefasCarregadas();
            }

            @Override
            public void onErro(String mensagem) {
                super.onErro(mensagem);
                marcarTarefasCarregadas();
            }
        });
    }

    private void marcarUsuarioCarregado() {
        usuarioCarregado = true;
        verificarCarregamentoCompleto();
    }

    private void marcarTarefasCarregadas() {
        tarefasCarregadas = true;
        verificarCarregamentoCompleto();
    }

    private void verificarCarregamentoCompleto() {
        if (usuarioCarregado && tarefasCarregadas) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    private void mostrarLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    // ---------- Conclusão (checkbox) ----------

    private void alterarConclusao(Tarefa tarefa, boolean concluida, int posicao) {

        // Atualiza a tela na hora (otimista) e desfaz se o servidor recusar.
        tarefa.setConcluida(concluida);
        tarefaAdapter.notifyItemChanged(posicao);
        atualizarProgresso();

        Resultado resultado = tarefaController.alterarConclusao(tarefa, concluida,
                new TelaCallback<Void>() {

                    @Override
                    public void onSucesso(Void dados) {
                        // A tela já está atualizada.
                    }

                    @Override
                    public void onErro(String mensagem) {
                        desfazerConclusao(tarefa, concluida);
                        super.onErro(mensagem);
                    }
                });

        if (!resultado.isSucesso()) {
            desfazerConclusao(tarefa, concluida);
            mostrarMensagem(resultado.getMensagem());
        }
    }

    private void desfazerConclusao(Tarefa tarefa, boolean valorTentado) {

        tarefa.setConcluida(!valorTentado);

        int posicao = tarefas.indexOf(tarefa);

        if (posicao >= 0) {
            tarefaAdapter.notifyItemChanged(posicao);
        }

        atualizarProgresso();
    }

    // ---------- Exclusão ----------

    private void mostrarConfirmacaoExclusao(Tarefa tarefa) {

        new AlertDialog.Builder(this)
                .setTitle("Excluir tarefa")
                .setMessage("Deseja excluir a tarefa \"" + tarefa.getTitulo() + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> excluirTarefa(tarefa))
                .show();
    }

    private void excluirTarefa(Tarefa tarefa) {

        Resultado resultado = tarefaController.excluir(tarefa, new TelaCallback<Void>() {

            @Override
            public void onSucesso(Void dados) {

                // Remove só o item, sem recarregar a lista inteira do servidor.
                int posicao = tarefas.indexOf(tarefa);

                if (posicao >= 0) {
                    tarefas.remove(posicao);
                    tarefaAdapter.notifyItemRemoved(posicao);
                }

                atualizarProgresso();
                atualizarEstadoVazio();
                mostrarMensagem("Tarefa excluída!");
            }
        });

        if (!resultado.isSucesso()) {
            mostrarMensagem(resultado.getMensagem());
        }
    }

    // ---------- Edição ----------

    private void abrirEdicaoTarefa(Tarefa tarefa) {
        Intent intent = new Intent(this, NovaTarefaActivity.class);
        intent.putExtra(NovaTarefaActivity.EXTRA_TAREFA, tarefa);
        startActivity(intent);
    }

    // ---------- Sair ----------

    private void mostrarConfirmacaoSaida() {

        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair da sua conta?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair", (dialog, which) -> sair())
                .show();
    }

    private void sair() {

        mostrarLoading();

        usuarioController.logout(new TelaCallback<Void>() {
            @Override
            public void onSucesso(Void dados) {
                irParaLogin(null);
            }
        });
    }

    // ---------- Resumo ----------

    private void atualizarProgresso() {

        int total = tarefas.size();
        int concluidas = 0;

        for (Tarefa tarefa : tarefas) {
            if (tarefa.isConcluida()) {
                concluidas++;
            }
        }

        txtProgresso.setText(concluidas + "/" + total);
        progressBar.setProgress(total > 0 ? (concluidas * 100) / total : 0);
    }

    private void atualizarEstadoVazio() {
        txtSemTarefas.setVisibility(tarefas.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
