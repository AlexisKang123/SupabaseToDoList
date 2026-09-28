package br.com.todolistsupabase.core.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Converte a resposta do Retrofit para o ResultadoCallback do app.
 * Todo o tratamento de código HTTP fica aqui, e não repetido em cada tela.
 */
public class RespostaCallback<T> implements Callback<T> {

    private static final String TAG = "RespostaCallback";

    private final ResultadoCallback<T> destino;

    public RespostaCallback(ResultadoCallback<T> destino) {
        this.destino = destino;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {

        if (!destino.podeReceber()) {
            return;
        }

        if (response.isSuccessful()) {
            destino.onSucesso(response.body());
        } else if (response.code() == 401) {
            destino.onSessaoExpirada();
        } else {
            destino.onErro(extrairMensagem(response));
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {

        if (call.isCanceled() || !destino.podeReceber()) {
            return;
        }

        Log.w(TAG, "Falha de conexão", t);
        destino.onErro("Sem conexão com o servidor. Verifique sua internet.");
    }

    /** Lê o JSON de erro do Supabase e traduz os casos mais comuns. */
    static String extrairMensagem(Response<?> response) {

        String padrao = "Erro no servidor (código " + response.code() + ").";

        try (ResponseBody corpo = response.errorBody()) {

            if (corpo == null) {
                return padrao;
            }

            String texto = corpo.string();
            Log.w(TAG, "Erro HTTP " + response.code() + ": " + texto);

            JsonObject json = JsonParser.parseString(texto).getAsJsonObject();
            String codigo = lerTexto(json, "error_code");

            if (codigo == null) {
                return padrao;
            }

            switch (codigo) {
                case "invalid_credentials":
                    return "E-mail ou senha inválidos.";
                case "email_not_confirmed":
                    return "Confirme seu e-mail antes de entrar.";
                case "user_already_exists":
                case "email_exists":
                    return "Este e-mail já está cadastrado.";
                case "weak_password":
                    return "Senha muito fraca. Escolha uma senha mais forte.";
                case "email_address_invalid":
                    return "E-mail inválido.";
                case "over_email_send_rate_limit":
                case "over_request_rate_limit":
                    return "Muitas tentativas. Aguarde alguns minutos.";
                default:
                    return padrao;
            }

        } catch (Exception e) {
            return padrao;
        }
    }

    private static String lerTexto(JsonObject json, String chave) {
        JsonElement valor = json.get(chave);
        return (valor != null && valor.isJsonPrimitive()) ? valor.getAsString() : null;
    }
}
