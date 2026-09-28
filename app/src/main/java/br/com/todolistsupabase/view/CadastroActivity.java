package br.com.todolistsupabase.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import br.com.todolistsupabase.R;
import br.com.todolistsupabase.controller.UsuarioController;
import br.com.todolistsupabase.model.Resultado;

public class CadastroActivity extends BaseActivity {

    private EditText edtNome;
    private EditText edtEmail;
    private EditText edtSenha;
    private TextView txtEntrar;
    private TextView btnCriarConta;

    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        usuarioController = new UsuarioController(this);

        inicializarComponentes();
        configurarEventos();
    }

    private void inicializarComponentes() {
        edtNome = findViewById(R.id.edtNomeCadastro);
        edtEmail = findViewById(R.id.edtEmailCadastro);
        edtSenha = findViewById(R.id.edtSenhaCadastro);
        txtEntrar = findViewById(R.id.txtEntrar);
        btnCriarConta = findViewById(R.id.txtCriarConta);
    }

    private void configurarEventos() {
        txtEntrar.setOnClickListener(view -> finish());
        btnCriarConta.setOnClickListener(view -> cadastrarUsuario());
    }

    private void cadastrarUsuario() {

        btnCriarConta.setEnabled(false);

        Resultado resultado = usuarioController.cadastrar(
                edtNome.getText().toString(),
                edtEmail.getText().toString(),
                edtSenha.getText().toString(),
                new TelaCallback<Boolean>() {

                    @Override
                    public void onSucesso(Boolean jaLogado) {

                        if (Boolean.TRUE.equals(jaLogado)) {
                            mostrarMensagem("Conta criada com sucesso!");
                            abrirTelaPrincipal();
                        } else {
                            mostrarMensagem("Conta criada! Confirme seu e-mail antes de entrar.");
                            finish();
                        }
                    }

                    @Override
                    public void onErro(String mensagem) {
                        btnCriarConta.setEnabled(true);
                        super.onErro(mensagem);
                    }
                }
        );

        if (!resultado.isSucesso()) {
            btnCriarConta.setEnabled(true);
            mostrarErroNoCampo(resultado);
        }
    }

    private void mostrarErroNoCampo(Resultado resultado) {

        EditText campo;

        switch (resultado.getCampo()) {
            case NOME:
                campo = edtNome;
                break;
            case EMAIL:
                campo = edtEmail;
                break;
            case SENHA:
                campo = edtSenha;
                break;
            default:
                mostrarMensagem(resultado.getMensagem());
                return;
        }

        campo.setError(resultado.getMensagem());
        campo.requestFocus();
    }

    private void abrirTelaPrincipal() {
        // Limpa a pilha: o login que está embaixo não deve voltar com o "voltar".
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
