package com.univasf.sistemaVendas;

import java.time.LocalDate;

public class Venda {
    private Cliente cliente;
    private Produto produto;
    private int quantidade;
    private double valorTotal;
    private LocalDate data;
    
    // Mantenha esses campos para compatibilidade
    int produtoId;
    int qtdProd;
    double valor;

    public Venda(Cliente cliente, Produto produto, int quantidade, LocalDate data) {
        this.cliente = cliente;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorTotal = produto.preco * quantidade;
        this.data = data;
        
        // Campos para compatibilidade
        this.produtoId = produto.id;
        this.qtdProd = quantidade;
        this.valor = valorTotal;
    }

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

    @Override
    public String toString() {
        return "Cliente: " + cliente.nome + ", Produto: " + produto.nome + 
               ", Quantidade: " + quantidade + ", Valor: " + valorTotal + 
               ", Data: " + data;
    }
}