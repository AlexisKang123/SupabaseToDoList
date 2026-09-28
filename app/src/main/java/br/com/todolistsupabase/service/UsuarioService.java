package br.com.todolistsupabase.service;

import android.content.Context;
import android.util.Patterns;

import br.com.todolistsupabase.core.network.RepassadorCallback;
import br.com.todolistsupabase.core.network.ResultadoCallback;
import br.com.todolistsupabase.core.session.SessaoManager;
import br.com.todolistsupabase.model.CadastroRequest;
import br.com.todolistsupabase.model.LoginRequest;
import br.com.todolistsupabase.model.LoginResponse;
import br.com.todolistsupabase.model.Resultado;
import br.com.todolistsupabase.model.Resultado.Campo;
import br.com.todolistsupabase.model.SupabaseUser;
import br.com.todolistsupabase.repository.UsuarioRepository;

// Regras de negócio do usuário. Também é quem salva e limpa a sessão,
// para que as telas não precisem mexer no SessaoManager.
public class UsuarioService {

    private static final int MIN_SENHA = 6;
    private static final int MAX_NOME = 100;
    private static final int MAX_EMAIL = 255;

    private final UsuarioRepository usuarioRepository;
    private final SessaoManager sessaoManager;

    public UsuarioService(Context context) {
        usuarioRepository = new UsuarioRepository(context);
        sessaoManager = new SessaoManager(context.getApplicationContext());
    }

    public boolean estaLogado() {
        return sessaoManager.estaLogado();
    }

    /**
     * onSucesso(true)  = conta criada e usuário já logado (confirmação de e-mail desativada).
     * onSucesso(false) = conta criada, mas precisa confirmar o e-mail antes de entrar.
     */
    public Resultado cadastrar(CadastroRequest request, ResultadoCallback<Boolean> callback) {

        if (request == null) {
            return Resultado.erro(Campo.NENHUM, "Os dados do cadastro são obrigatórios.");
        }

        String nome = request.getNome();

        if (nome == null || nome.trim().isEmpty()) {
            return Resultado.erro(Campo.NOME, "O nome é obrigatório.");
        }

        if (nome.trim().length() > MAX_NOME) {
            return Resultado.erro(Campo.NOME,
                    "O nome pode ter no máximo " + MAX_NOME + " caracteres.");
        }

        Resultado validacao = validarCredenciais(request.getEmail(), request.getPassword());

        if (!validacao.isSucesso()) {
            return validacao;
        }

        if (request.getPassword().length() < MIN_SENHA) {
            return Resultado.erro(Campo.SENHA,
                    "A senha deve ter pelo menos " + MIN_SENHA + " caracteres.");
        }

        usuarioRepository.cadastrar(request, new RepassadorCallback<LoginResponse, Boolean>(callback) {
            @Override
            public void onSucesso(LoginResponse resposta) {
                boolean jaLogado = salvarSessaoSeHouver(resposta);
                destino.onSucesso(jaLogado);
            }
        });

        return Resultado.ok();
    }

    public Resultado login(LoginRequest request, ResultadoCallback<Void> callback) {

        if (request == null) {
            return Resultado.erro(Campo.NENHUM, "Os dados de login são obrigatórios.");
        }

        Resultado validacao = validarCredenciais(request.getEmail(), request.getPassword());

        if (!validacao.isSucesso()) {
            return validacao;
        }

        usuarioRepository.login(request, new RepassadorCallback<LoginResponse, Void>(callback) {
            @Override
            public void onSucesso(LoginResponse resposta) {
                if (salvarSessaoSeHouver(resposta)) {
                    destino.onSucesso(null);
                } else {
                    destino.onErro("Não foi possível iniciar a sessão. Tente novamente.");
                }
            }
        });

        return Resultado.ok();
    }

    /** Devolve o nome salvo no cadastro (user_metadata.name), ou null. */
    public void obterNome(ResultadoCallback<String> callback) {

        usuarioRepository.obterUsuario(new RepassadorCallback<SupabaseUser, String>(callback) {
            @Override
            public void onSucesso(SupabaseUser usuario) {
                String nome = null;
                if (usuario != null && usuario.getUserMetadata() != null) {
                    nome = usuario.getUserMetadata().getName();
                }
                destino.onSucesso(nome);
            }
        });
    }

    /** Invalida a sessão no servidor e SEMPRE limpa a sessão local, mesmo sem internet. */
    public void logout(ResultadoCallback<Void> callback) {

        usuarioRepository.logout(new RepassadorCallback<Void, Void>(callback) {
            @Override
            public void onSucesso(Void dados) {
                finalizar();
            }

            @Override
            public void onErro(String mensagem) {
                finalizar();
            }

            @Override
            public void onSessaoExpirada() {
                finalizar();
            }

            private void finalizar() {
                sessaoManager.limparSessao();
                destino.onSucesso(null);
            }
        });
    }

    private Resultado validarCredenciais(String email, String senha) {

        if (email == null || email.trim().isEmpty()) {
            return Resultado.erro(Campo.EMAIL, "O e-mail é obrigatório.");
        }

        if (email.trim().length() > MAX_EMAIL) {
            return Resultado.erro(Campo.EMAIL, "O e-mail é muito longo.");
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return Resultado.erro(Campo.EMAIL, "Digite um e-mail válido.");
        }

        if (senha == null || senha.trim().isEmpty()) {
            return Resultado.erro(Campo.SENHA, "A senha é obrigatória.");
        }

        return Resultado.ok();
    }

    private boolean salvarSessaoSeHouver(LoginResponse resposta) {

        if (resposta == null || resposta.getAccessToken() == null || resposta.getUser() == null) {
            return false;
        }

        sessaoManager.salvarSessao(
                resposta.getAccessToken(),
                resposta.getRefreshToken(),
                resposta.getUser().getId()
        );

        return true;
    }
}
