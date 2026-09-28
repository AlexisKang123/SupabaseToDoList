package br.com.todolistsupabase.core.network;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import br.com.todolistsupabase.core.session.SessaoManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adiciona "Authorization: Bearer <token>" automaticamente quando existe sessão.
 * As rotas de login e cadastro são públicas e nunca recebem o token.
 */
public class AuthInterceptor implements Interceptor {

    private final SessaoManager sessaoManager;

    public AuthInterceptor(Context context) {
        sessaoManager = new SessaoManager(context.getApplicationContext());
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        // Já tem token (ex.: requisição refeita pelo TokenAuthenticator) ou é rota pública
        if (original.header("Authorization") != null || ehRotaPublica(original)) {
            return chain.proceed(original);
        }

        String accessToken = sessaoManager.getAccessToken();

        if (accessToken == null || accessToken.trim().isEmpty()) {
            return chain.proceed(original);
        }

        Request comToken = original.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();

        return chain.proceed(comToken);
    }

    static boolean ehRotaPublica(Request requisicao) {
        String caminho = requisicao.url().encodedPath();
        return caminho.endsWith("/auth/v1/token") || caminho.endsWith("/auth/v1/signup");
    }
}
