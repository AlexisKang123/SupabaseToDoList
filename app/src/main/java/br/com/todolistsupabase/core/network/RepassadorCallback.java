package br.com.todolistsupabase.core.network;

/**
 * Callback intermediário: trata o sucesso do seu jeito (ex.: salvar a sessão)
 * e repassa erro e sessão expirada para o callback de destino.
 */
public abstract class RepassadorCallback<A, B> implements ResultadoCallback<A> {

    protected final ResultadoCallback<B> destino;

    protected RepassadorCallback(ResultadoCallback<B> destino) {
        this.destino = destino;
    }

    @Override
    public void onErro(String mensagem) {
        destino.onErro(mensagem);
    }

    @Override
    public void onSessaoExpirada() {
        destino.onSessaoExpirada();
    }

    @Override
    public boolean podeReceber() {
        return destino.podeReceber();
    }
}
