package br.com.todolistsupabase.core.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import br.com.todolistsupabase.core.config.SupabaseConfig;
import br.com.todolistsupabase.core.session.SessaoManager;
import br.com.todolistsupabase.model.LoginResponse;
import br.com.todolistsupabase.model.RefreshTokenRequest;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Route;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Chamado pelo OkHttp quando o servidor responde 401.
 * Tenta renovar o access token com o refresh token e refaz a requisição.
 */
public class TokenAuthenticator implements Authenticator {

    private final SessaoManager sessaoManager;
    private final AuthApi authApi;

    public TokenAuthenticator(Context context) {

        sessaoManager = new SessaoManager(context.getApplicationContext());

        // Cliente separado, SEM authenticator, para o refresh não cair em loop.
        OkHttpClient clienteRefresh = new OkHttpClient.Builder()
                .addInterceptor(new ApiKeyInterceptor())
                .build();

        authApi = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.BASE_URL)
                .client(clienteRefresh)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi.class);
    }

    // "synchronized": se duas requisições recebem 401 juntas, só uma renova o token.
    @Nullable
    @Override
    public synchronized Request authenticate(@Nullable Route route,
                                             @NonNull okhttp3.Response response) throws IOException {

        // Login e cadastro não usam token: um 401 ali não é sessão expirada.
        if (AuthInterceptor.ehRotaPublica(response.request())) {
            return null;
        }

        // Já tentamos uma vez e continuou 401: desiste.
        if (contarRespostas(response) >= 2) {
            return null;
        }

        String tokenAtual = sessaoManager.getAccessToken();
        String authorizationUsado = response.request().header("Authorization");

        // Enquanto esta requisição esperava, outra já renovou o token: só reenvia.
        if (tokenAtual != null && !("Bearer " + tokenAtual).equals(authorizationUsado)) {
            return comToken(response.request(), tokenAtual);
        }

        String refreshToken = sessaoManager.getRefreshToken();

        if (estaVazio(refreshToken)) {
            sessaoManager.limparSessao();
            return null;
        }

        // Se não houver internet, a IOException sobe e o app mostra "erro de conexão"
        // em vez de derrubar a sessão do usuário.
        Response<LoginResponse> refreshResponse = authApi
                .atualizarToken("refresh_token", new RefreshTokenRequest(refreshToken))
                .execute();

        LoginResponse corpo = refreshResponse.body();

        if (!refreshResponse.isSuccessful() || corpo == null || estaVazio(corpo.getAccessToken())) {
            // 4xx = refresh token inválido/revogado: a sessão acabou de verdade.
            if (refreshResponse.code() >= 400 && refreshResponse.code() < 500) {
                sessaoManager.limparSessao();
            }
            return null;
        }

        String novoRefreshToken = estaVazio(corpo.getRefreshToken())
                ? refreshToken
                : corpo.getRefreshToken();

        sessaoManager.salvarSessao(
                corpo.getAccessToken(),
                novoRefreshToken,
                sessaoManager.getUsuarioId()
        );

        return comToken(response.request(), corpo.getAccessToken());
    }

    private Request comToken(Request requisicao, String accessToken) {
        return requisicao.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
    }

    private int contarRespostas(okhttp3.Response response) {
        int quantidade = 1;
        while ((response = response.priorResponse()) != null) {
            quantidade++;
        }
        return quantidade;
    }

    private boolean estaVazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
