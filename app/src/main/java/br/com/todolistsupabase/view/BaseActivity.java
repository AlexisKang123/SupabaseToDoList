package br.com.todolistsupabase.view;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.todolistsupabase.core.network.ResultadoCallback;
import br.com.todolistsupabase.core.session.SessaoManager;

/**
 * Comportamento comum a todas as telas:
 * - mostrar mensagens;
 * - voltar ao login quando a sessão expira;
 * - ignorar respostas que chegam depois que a tela foi fechada.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void mostrarMensagem(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    protected void irParaLogin(String mensagem) {

        new SessaoManager(this).limparSessao();

        if (mensagem != null) {
            Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Callback padrão das telas. Só é preciso implementar onSucesso;
     * erro e sessão expirada já têm um tratamento padrão que pode ser sobrescrito.
     */
    protected abstract class TelaCallback<T> implements ResultadoCallback<T> {

        @Override
        public void onErro(String mensagem) {
            mostrarMensagem(mensagem);
        }

        @Override
        public void onSessaoExpirada() {
            irParaLogin("Sua sessão expirou. Entre novamente.");
        }

        @Override
        public boolean podeReceber() {
            return !isFinishing() && !isDestroyed();
        }
    }
}
