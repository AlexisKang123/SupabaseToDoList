package br.com.todolistsupabase.core.network;

import android.content.Context;

import br.com.todolistsupabase.core.config.SupabaseConfig;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    // Por que usamos getInstance()?
    // Porque não precisamos criar um Retrofit novo toda vez que quisermos fazer uma requisição.
    // "synchronized" garante que duas threads não criem duas instâncias ao mesmo tempo.
    public static synchronized Retrofit getInstance(Context context) {

        if (retrofit == null) {

            Context appContext = context.getApplicationContext();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new ApiKeyInterceptor())         // header "apikey"
                    .addInterceptor(new AuthInterceptor(appContext)) // header "Authorization"
                    .authenticator(new TokenAuthenticator(appContext)) // renova o token no 401
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
