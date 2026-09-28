package br.com.todolistsupabase.core.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import br.com.todolistsupabase.core.config.SupabaseConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adiciona o header "apikey" em TODAS as requisições.
 * Assim nenhuma interface do Retrofit precisa receber a chave como parâmetro.
 */
public class ApiKeyInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request requisicao = chain.request()
                .newBuilder()
                .header("apikey", SupabaseConfig.API_KEY)
                .build();

        return chain.proceed(requisicao);
    }
}
