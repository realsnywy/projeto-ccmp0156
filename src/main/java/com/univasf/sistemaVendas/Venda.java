package com.univasf.sistemaVendas;

import java.time.LocalDate;

public class Venda {
    // Atributos principais da classe Venda
    private Cliente cliente; // Cliente associado à venda
    private Produto produto; // Produto vendido
    private int quantidade; // Quantidade do produto vendido
    private double valorTotal; // Valor total da venda
    private LocalDate data; // Data da venda

    // Campos mantidos para compatibilidade com versões anteriores
    int produtoId; // ID do produto (compatibilidade)
    int qtdProd; // Quantidade do produto (compatibilidade)
    double valor; // Valor total da venda (compatibilidade)

    // Construtor da classe Venda
    public Venda(Cliente cliente, Produto produto, int quantidade, LocalDate data) {
        this.cliente = cliente;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorTotal = produto.getPreco() * quantidade; // Calcula o valor total da venda
        this.data = data;

        // Inicializa os campos de compatibilidade
        this.produtoId = produto.getId();
        this.qtdProd = quantidade;
        this.valor = valorTotal;
    }

    // Métodos getters para acessar os atributos da classe
    public Cliente getCliente() {
        return cliente;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public LocalDate getData() {
        return data;
    }

    // Método toString para representar a venda como uma string
    @Override
    public String toString() {
        return "Cliente: " + cliente.getNome() + ", Produto: " + produto.getNome() +
                ", Quantidade: " + quantidade + ", Valor: " + valorTotal +
                ", Data: " + data;
    }
}