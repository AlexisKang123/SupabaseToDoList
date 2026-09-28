package br.com.todolistsupabase.controller;

import android.content.Context;

import br.com.todolistsupabase.core.network.ResultadoCallback;
import br.com.todolistsupabase.model.CadastroRequest;
import br.com.todolistsupabase.model.LoginRequest;
import br.com.todolistsupabase.model.Resultado;
import br.com.todolistsupabase.service.UsuarioService;

// Recebe os textos da tela, monta os requests e chama o service.
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(Context context) {
        usuarioService = new UsuarioService(context);
    }

    public boolean estaLogado() {
        return usuarioService.estaLogado();
    }

    public Resultado cadastrar(String nome, String email, String senha,
                               ResultadoCallback<Boolean> callback) {

        CadastroRequest request = new CadastroRequest(limpar(email), senha, limpar(nome));
        return usuarioService.cadastrar(request, callback);
    }

    public Resultado login(String email, String senha, ResultadoCallback<Void> callback) {
        return usuarioService.login(new LoginRequest(limpar(email), senha), callback);
    }

    public void obterNome(ResultadoCallback<String> callback) {
        usuarioService.obterNome(callback);
    }

    public void logout(ResultadoCallback<Void> callback) {
        usuarioService.logout(callback);
    }

    private String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
