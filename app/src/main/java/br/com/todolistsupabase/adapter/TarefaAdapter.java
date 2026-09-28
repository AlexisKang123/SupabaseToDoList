package br.com.todolistsupabase.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.todolistsupabase.R;
import br.com.todolistsupabase.core.util.DataUtils;
import br.com.todolistsupabase.model.Tarefa;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    public interface OnConclusaoAlterada {
        void onConclusaoAlterada(Tarefa tarefa, boolean concluida, int posicao);
    }

    public interface OnTarefaLongClick {
        void onLongClick(Tarefa tarefa);
    }

    public interface OnTarefaClick {
        void onClick(Tarefa tarefa);
    }

    private final List<Tarefa> tarefas;
    private final OnConclusaoAlterada conclusaoListener;
    private final OnTarefaLongClick longClickListener;
    private final OnTarefaClick clickListener;

    public TarefaAdapter(List<Tarefa> tarefas,
                         OnConclusaoAlterada conclusaoListener,
                         OnTarefaLongClick longClickListener,
                         OnTarefaClick clickListener) {
        this.tarefas = tarefas;
        this.conclusaoListener = conclusaoListener;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {

        Tarefa tarefa = tarefas.get(position);

        holder.txtTitulo.setText(tarefa.getTitulo());
        holder.txtDescricao.setText(tarefa.getDescricao());
        holder.txtData.setText(DataUtils.paraTela(tarefa.getData()));
        holder.txtPrioridade.setText("Prioridade: " + obterTextoPrioridade(tarefa.getPrioridade()));

        // Remove o listener antes do setChecked para ele não disparar na reciclagem.
        holder.checkConcluida.setOnCheckedChangeListener(null);
        holder.checkConcluida.setChecked(tarefa.isConcluida());

        riscarTexto(holder.txtTitulo, tarefa.isConcluida());
        riscarTexto(holder.txtDescricao, tarefa.isConcluida());

        holder.checkConcluida.setOnCheckedChangeListener((botao, marcado) -> {

            // Posição atual do item (pode ter mudado desde o bind).
            int posicaoAtual = holder.getBindingAdapterPosition();

            if (conclusaoListener != null && posicaoAtual != RecyclerView.NO_POSITION) {
                conclusaoListener.onConclusaoAlterada(tarefas.get(posicaoAtual), marcado, posicaoAtual);
            }
        });

        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onClick(tarefa);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(tarefa);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tarefas.size();
    }

    private void riscarTexto(TextView texto, boolean riscar) {
        int flags = texto.getPaintFlags();
        texto.setPaintFlags(riscar
                ? flags | Paint.STRIKE_THRU_TEXT_FLAG
                : flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private String obterTextoPrioridade(int prioridade) {
        switch (prioridade) {
            case Tarefa.PRIORIDADE_BAIXA:
                return "Baixa";
            case Tarefa.PRIORIDADE_MEDIA:
                return "Média";
            case Tarefa.PRIORIDADE_ALTA:
                return "Alta";
            default:
                return "Desconhecida";
        }
    }

    public static class TarefaViewHolder extends RecyclerView.ViewHolder {

        final CheckBox checkConcluida;
        final TextView txtTitulo;
        final TextView txtDescricao;
        final TextView txtData;
        final TextView txtPrioridade;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            checkConcluida = itemView.findViewById(R.id.checkAtivo);
            txtTitulo = itemView.findViewById(R.id.txtTituloLista);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtData = itemView.findViewById(R.id.txtData);
            txtPrioridade = itemView.findViewById(R.id.txtPrioridade);
        }
    }
}
