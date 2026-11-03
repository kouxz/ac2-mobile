package com.projeto.ac2;

import com.google.firebase.firestore.DocumentId;

public class Livro {
    @DocumentId
    private String id;
    private String titulo;
    private String autor;
    private String genero;
    private int anoPublicacao;
    private String statusLeitura;
    private boolean favorito;

    public Livro() {}

    public Livro(String id, String titulo, String autor, String genero, int anoPublicacao, String statusLeitura, boolean favorito) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.anoPublicacao = anoPublicacao;
        this.statusLeitura = statusLeitura;
        this.favorito = favorito;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getId() {
        return id;
    }

    public String getAutor() {
        return autor;
    }

    public String getGenero() {
        return genero;
    }

    public int getAnoPublicacao() {
        return anoPublicacao;
    }

    public String getStatusLeitura() {
        return statusLeitura;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public void setAnoPublicacao(int anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public void setStatusLeitura(String statusLeitura) {
        this.statusLeitura = statusLeitura;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
}
