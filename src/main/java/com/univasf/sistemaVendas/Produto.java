package com.univasf.sistemaVendas;

class Produto {
    int id;
    String nome;
    double preco;
    String tipo;

    public Produto(int id, String nome, double preco, String tipo) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return id + ", " + nome + ", Preço: " + preco + ", Tipo: " + tipo;
    }
}
