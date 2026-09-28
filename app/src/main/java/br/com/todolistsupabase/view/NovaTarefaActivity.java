package br.com.todolistsupabase.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.core.content.IntentCompat;

import java.util.Calendar;

import br.com.todolistsupabase.R;
import br.com.todolistsupabase.controller.TarefaController;
import br.com.todolistsupabase.core.util.DataUtils;
import br.com.todolistsupabase.model.Resultado;
import br.com.todolistsupabase.model.Tarefa;

// Tela usada para criar (sem extra) e para editar (com EXTRA_TAREFA).
public class NovaTarefaActivity extends BaseActivity {

    public static final String EXTRA_TAREFA = "tarefa";

    private EditText edtDataTarefa, edtDescricaoTarefa, edtTituloTarefa;
    private CheckBox checkPrioridadeAlta, checkPrioridadeMedia, checkPrioridadeBaixa;
    private Button btnSalvar;

    private Tarefa tarefaEmEdicao; // null = criando uma tarefa nova
    private TarefaController tarefaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_tarefa);

        tarefaEmEdicao = IntentCompat.getSerializableExtra(getIntent(), EXTRA_TAREFA, Tarefa.class);
        tarefaController = new TarefaController(this);

        inicializarComponentes();
        preencherDadosTarefa();
        configurarEventos();
    }

    private boolean estaEditando() {
        return tarefaEmEdicao != null;
    }

    private void inicializarComponentes() {
        edtTituloTarefa = findViewById(R.id.edtTituloTarefa);
        edtDescricaoTarefa = findViewById(R.id.edtDescricaoTarefa);
        edtDataTarefa = findViewById(R.id.edtDataTarefa);
        checkPrioridadeAlta = findViewById(R.id.checkPrioridadeAlta);
        checkPrioridadeMedia = findViewById(R.id.checkPrioridadeMedia);
        checkPrioridadeBaixa = findViewById(R.id.checkPrioridadeBaixa);
        btnSalvar = findViewById(R.id.btnSalvar);
    }

    private void preencherDadosTarefa() {

        if (!estaEditando()) {
            checkPrioridadeMedia.setChecked(true);
            return;
        }

        edtTituloTarefa.setText(tarefaEmEdicao.getTitulo());
        edtDescricaoTarefa.setText(tarefaEmEdicao.getDescricao());
        edtDataTarefa.setText(DataUtils.paraTela(tarefaEmEdicao.getData()));

        switch (tarefaEmEdicao.getPrioridade()) {
            case Tarefa.PRIORIDADE_ALTA:
                checkPrioridadeAlta.setChecked(true);
                break;
            case Tarefa.PRIORIDADE_BAIXA:
                checkPrioridadeBaixa.setChecked(true);
                break;
            default:
                checkPrioridadeMedia.setChecked(true);
        }

        btnSalvar.setText("Atualizar tarefa");
    }

    private void configurarEventos() {

        edtDataTarefa.setOnClickListener(view -> mostrarSeletorData());
        btnSalvar.setOnClickListener(view -> salvar());

        // Os três checkboxes funcionam como um grupo: sempre exatamente um marcado.
        CompoundButton.OnCheckedChangeListener grupoPrioridade = (botao, marcado) -> {

            if (marcado) {
                if (botao != checkPrioridadeAlta) checkPrioridadeAlta.setChecked(false);
                if (botao != checkPrioridadeMedia) checkPrioridadeMedia.setChecked(false);
                if (botao != checkPrioridadeBaixa) checkPrioridadeBaixa.setChecked(false);
            } else if (!algumaPrioridadeMarcada()) {
                botao.setChecked(true); // impede desmarcar todos
            }
        };

        checkPrioridadeAlta.setOnCheckedChangeListener(grupoPrioridade);
        checkPrioridadeMedia.setOnCheckedChangeListener(grupoPrioridade);
        checkPrioridadeBaixa.setOnCheckedChangeListener(grupoPrioridade);
    }

    private boolean algumaPrioridadeMarcada() {
        return checkPrioridadeAlta.isChecked()
                || checkPrioridadeMedia.isChecked()
                || checkPrioridadeBaixa.isChecked();
    }

    private int obterPrioridade() {
        if (checkPrioridadeAlta.isChecked()) return Tarefa.PRIORIDADE_ALTA;
        if (checkPrioridadeBaixa.isChecked()) return Tarefa.PRIORIDADE_BAIXA;
        return Tarefa.PRIORIDADE_MEDIA;
    }

    private void salvar() {

        // Evita tarefa duplicada por clique duplo.
        btnSalvar.setEnabled(false);

        String titulo = edtTituloTarefa.getText().toString();
        String descricao = edtDescricaoTarefa.getText().toString();
        String data = edtDataTarefa.getText().toString();
        int prioridade = obterPrioridade();

        TelaCallback<Void> callback = new TelaCallback<Void>() {

            @Override
            public void onSucesso(Void dados) {
                mostrarMensagem(estaEditando() ? "Tarefa atualizada!" : "Tarefa criada!");
                finish();
            }

            @Override
            public void onErro(String mensagem) {
                btnSalvar.setEnabled(true);
                super.onErro(mensagem);
            }
        };

        Resultado resultado = estaEditando()
                ? tarefaController.editar(tarefaEmEdicao.getId(), titulo, descricao, data, prioridade, callback)
                : tarefaController.criar(titulo, descricao, data, prioridade, callback);

        if (!resultado.isSucesso()) {
            btnSalvar.setEnabled(true);
            mostrarErroNoCampo(resultado);
        }
    }

    private void mostrarErroNoCampo(Resultado resultado) {

        EditText campo;

        switch (resultado.getCampo()) {
            case TITULO:
                campo = edtTituloTarefa;
                break;
            case DESCRICAO:
                campo = edtDescricaoTarefa;
                break;
            case DATA:
                campo = edtDataTarefa;
                break;
            default:
                mostrarMensagem(resultado.getMensagem());
                return;
        }

        campo.setError(resultado.getMensagem());
        campo.requestFocus();
    }

    private void mostrarSeletorData() {

        Calendar calendar = DataUtils.paraCalendar(edtDataTarefa.getText().toString());

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, ano, mes, dia) -> {
                    edtDataTarefa.setText(DataUtils.formatarTela(ano, mes, dia));
                    edtDataTarefa.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }
}
