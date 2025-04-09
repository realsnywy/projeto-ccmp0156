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

    @Override
    public String toString() {
        return id + ", " + nome + ", Preço: " + preco + ", Tipo: " + tipo;
    }
}
