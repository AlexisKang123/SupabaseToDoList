package br.com.todolistsupabase.core.network;

/**
 * Callback do próprio app. A View recebe só isto e não conhece o Retrofit:
 * se um dia a biblioteca de rede mudar, as telas não precisam mudar.
 */
public interface ResultadoCallback<T> {

    void onSucesso(T dados);

    void onErro(String mensagem);

    /** A sessão acabou (token inválido e não foi possível renovar). */
    void onSessaoExpirada();

    /** Retorne false quando quem pediu não existe mais (ex.: Activity fechada). */
    default boolean podeReceber() {
        return true;
    }
}
