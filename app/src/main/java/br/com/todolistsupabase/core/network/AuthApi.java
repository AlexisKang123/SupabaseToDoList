package br.com.todolistsupabase.core.network;

import br.com.todolistsupabase.model.CadastroRequest;
import br.com.todolistsupabase.model.LoginRequest;
import br.com.todolistsupabase.model.LoginResponse;
import br.com.todolistsupabase.model.RefreshTokenRequest;
import br.com.todolistsupabase.model.SupabaseUser;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

// Os headers "apikey" e "Authorization" são adicionados pelos interceptors.
public interface AuthApi {

    // Se a confirmação de e-mail estiver DESATIVADA, a resposta já traz a sessão.
    // Se estiver ATIVADA, access_token vem nulo e o usuário precisa confirmar o e-mail.
    @POST("auth/v1/signup")
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);

    @POST("auth/v1/token")
    Call<LoginResponse> login(
            @Query("grant_type") String grantType,
            @Body LoginRequest request
    );

    @POST("auth/v1/token")
    Call<LoginResponse> atualizarToken(
            @Query("grant_type") String grantType,
            @Body RefreshTokenRequest request
    );

    @GET("auth/v1/user")
    Call<SupabaseUser> obterUsuario();

    // Invalida o refresh token no servidor.
    @POST("auth/v1/logout")
    Call<Void> logout();
}
