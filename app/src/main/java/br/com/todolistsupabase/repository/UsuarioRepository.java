package br.com.todolistsupabase.repository;

import android.content.Context;

import br.com.todolistsupabase.core.network.AuthApi;
import br.com.todolistsupabase.core.network.RespostaCallback;
import br.com.todolistsupabase.core.network.ResultadoCallback;
import br.com.todolistsupabase.core.network.RetrofitClient;
import br.com.todolistsupabase.model.CadastroRequest;
import br.com.todolistsupabase.model.LoginRequest;
import br.com.todolistsupabase.model.LoginResponse;
import br.com.todolistsupabase.model.SupabaseUser;

public class UsuarioRepository {

    private final AuthApi authApi;

    public UsuarioRepository(Context context) {
        authApi = RetrofitClient.getInstance(context).create(AuthApi.class);
    }

    public void cadastrar(CadastroRequest request, ResultadoCallback<LoginResponse> callback) {
        authApi.cadastrar(request).enqueue(new RespostaCallback<>(callback));
    }

    public void login(LoginRequest request, ResultadoCallback<LoginResponse> callback) {
        authApi.login("password", request).enqueue(new RespostaCallback<>(callback));
    }

    public void obterUsuario(ResultadoCallback<SupabaseUser> callback) {
        authApi.obterUsuario().enqueue(new RespostaCallback<>(callback));
    }

    public void logout(ResultadoCallback<Void> callback) {
        authApi.logout().enqueue(new RespostaCallback<>(callback));
    }
}
