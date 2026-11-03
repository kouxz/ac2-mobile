package com.projeto.ac2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.LivroViewHolder> {

    private List<Livro> listaLivros;
    private AoClicarNoItem ouvinteCliques;

    public interface AoClicarNoItem {
        void aoClicar(Livro livro); // Clique curto -> Editar
        void aoClicarLongo(Livro livro); // Clique longo -> Excluir
    }

    public LivroAdapter(List<Livro> listaLivros, AoClicarNoItem ouvinteCliques) {
        this.listaLivros = listaLivros;
        this.ouvinteCliques = ouvinteCliques;
    }

    public void atualizarLista(List<Livro> novaLista) {
        this.listaLivros = novaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livro, parent, false);
        return new LivroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LivroViewHolder holder, int position) {
        Livro livro = listaLivros.get(position);
        holder.tvTitulo.setText(livro.getTitulo());
        holder.tvAutor.setText("Autor: " + livro.getAutor());
        holder.tvGenero.setText("Gênero: " + livro.getGenero());
        holder.tvAno.setText("Ano: " + livro.getAnoPublicacao());
        holder.tvStatus.setText("Status: " + livro.getStatusLeitura());
        holder.tvFavorito.setText(livro.isFavorito() ? "⭐ Favorito" : "");

        holder.itemView.setOnClickListener(v -> ouvinteCliques.aoClicar(livro));
        holder.itemView.setOnLongClickListener(v -> {
            ouvinteCliques.aoClicarLongo(livro);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaLivros.size();
    }

    static class LivroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor, tvGenero, tvAno, tvStatus, tvFavorito;

        public LivroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            tvAutor = itemView.findViewById(R.id.tvItemAutor);
            tvGenero = itemView.findViewById(R.id.tvItemGenero);
            tvAno = itemView.findViewById(R.id.tvItemAno);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
            tvFavorito = itemView.findViewById(R.id.tvItemFavorito);
        }
    }
}
