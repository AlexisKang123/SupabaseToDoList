package br.com.todolistsupabase.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import br.com.todolistsupabase.R;
import br.com.todolistsupabase.controller.UsuarioController;
import br.com.todolistsupabase.model.Resultado;

public class LoginActivity extends BaseActivity {

    private EditText edtEmail, edtSenha;
    private Button btnEntrar;
    private TextView txtCriarConta;

    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usuarioController = new UsuarioController(this);

        // Verifica ANTES de montar a tela: evita que o login "pisque"
        // e interrompe o onCreate com return.
        if (usuarioController.estaLogado()) {
            abrirTelaPrincipal();
            return;
        }

        setContentView(R.layout.activity_login);

        inicializarComponentes();
        configurarEventos();
    }

    private void inicializarComponentes() {
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        txtCriarConta = findViewById(R.id.txtCriarConta);
    }

    private void configurarEventos() {

        btnEntrar.setOnClickListener(view -> fazerLogin());

        txtCriarConta.setOnClickListener(view ->
                startActivity(new Intent(this, CadastroActivity.class)));
    }

    private void fazerLogin() {

        // Evita clique duplo enquanto a requisição está em andamento.
        btnEntrar.setEnabled(false);

        Resultado resultado = usuarioController.login(
                edtEmail.getText().toString(),
                edtSenha.getText().toString(),
                new TelaCallback<Void>() {

                    @Override
                    public void onSucesso(Void dados) {
                        abrirTelaPrincipal();
                    }

                    @Override
                    public void onErro(String mensagem) {
                        btnEntrar.setEnabled(true);
                        super.onErro(mensagem);
                    }
                }
        );

        if (!resultado.isSucesso()) {
            btnEntrar.setEnabled(true);
            mostrarErroNoCampo(resultado);
        }
    }

    private void mostrarErroNoCampo(Resultado resultado) {

        EditText campo = resultado.getCampo() == Resultado.Campo.EMAIL ? edtEmail : edtSenha;

        campo.setError(resultado.getMensagem());
        campo.requestFocus();
    }

    private void abrirTelaPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
