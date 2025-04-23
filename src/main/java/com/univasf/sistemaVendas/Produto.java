package com.univasf.sistemaVendas;

// Classe que representa um produto no sistema de vendas
class Produto {
    // Atributos da classe Produto
    int id; // Identificador único do produto
    private String nome; // Nome do produto
    double preco; // Preço do produto
    private String tipo; // Tipo ou categoria do produto

    // Construtor da classe Produto
    public Produto(int id, String nome, double preco, String tipo) {
        this.id = id; // Inicializa o ID do produto
        this.nome = nome; // Inicializa o nome do produto
        this.preco = preco; // Inicializa o preço do produto
        this.tipo = tipo; // Inicializa o tipo do produto
    }

    // Métodos Getters e Setters para acessar e modificar os atributos

    public int getId() {
        return id; // Retorna o ID do produto
    }

    public void setId(int id) {
        this.id = id; // Define o ID do produto
    }

    public String getNome() {
        return nome; // Retorna o nome do produto
    }

    public void setNome(String nome) {
        this.nome = nome; // Define o nome do produto
    }

    public double getPreco() {
        return preco; // Retorna o preço do produto
    }

    public void setPreco(double preco) {
        this.preco = preco; // Define o preço do produto
    }

    public String getTipo() {
        return tipo; // Retorna o tipo do produto
    }

    public void setTipo(String tipo) {
        this.tipo = tipo; // Define o tipo do produto
    }

    // Método sobrescrito para retornar uma representação em String do produto
    @Override
    public String toString() {
        return id + ", " + nome + ", Preço: " + preco + ", Tipo: " + tipo;
    }
}