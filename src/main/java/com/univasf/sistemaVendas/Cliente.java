package com.univasf.sistemaVendas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Cliente {
 int id;
 String nome, email, telefone;
 int compras;
 double totalGasto;
 List<Venda> historicoCompras;

 public Cliente(int id, String nome, String email, String telefone) {
     this.id = id;
     this.nome = nome;
     this.email = email;
     this.telefone = telefone;
     this.compras = 0;
     this.totalGasto = 0.0;
     this.historicoCompras = new ArrayList<>();
 }

 public void registrarCompra(Venda venda) {
     compras++;
     totalGasto += venda.valor;
     historicoCompras.add(venda);
 }

 public Map<Integer, Integer> getProdutosMaisComprados() {
     Map<Integer, Integer> produtosQuantidade = new HashMap<>();
     for (Venda venda : historicoCompras) {
         produtosQuantidade.merge(venda.produtoId, venda.qtdProd, Integer::sum);
     }
     return produtosQuantidade;
 }

 public List<Integer> sugerirProdutosSimilares(List<Produto> todosProdutos) {
     Map<Integer, Integer> produtosComprados = getProdutosMaisComprados();
     if (produtosComprados.isEmpty()) {
         return todosProdutos.stream()
                 .sorted((p1, p2) -> Double.compare(p2.preco, p1.preco))
                 .limit(3)
                 .map(p -> p.id)
                 .collect(Collectors.toList());
     }
     
     int produtoMaisComprado = produtosComprados.entrySet().stream()
             .max(Map.Entry.comparingByValue())
             .get().getKey();
     
     return todosProdutos.stream()
             .filter(p -> p.id != produtoMaisComprado)
             .sorted((p1, p2) -> Double.compare(p2.preco, p1.preco))
             .limit(3)
             .map(p -> p.id)
             .collect(Collectors.toList());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public int getCompras() {
        return compras;
    }

    public void setCompras(int compras) {
        this.compras = compras;
    }

    public double getTotalGasto() {
        return totalGasto;
    }

    public void setTotalGasto(double totalGasto) {
        this.totalGasto = totalGasto;
    }

    public List<Venda> getHistoricoCompras() {
        return historicoCompras;
    }

    public void exibirHistoricoCompras() {
        if (historicoCompras.isEmpty()) {
            System.out.println("Nenhuma compra registrada para este cliente.");
        } else {
            System.out.println("Histórico de compras do cliente " + this.nome + ":");
            for (Venda venda : historicoCompras) {
                System.out.println(venda); // Certifique-se de que a classe Venda tem um toString() bem feito
            }
        }
    }

    @Override
 public String toString() {
     return id + ", " + nome + ", " + email + ", " + telefone + ", Compras: " + compras + ", Total Gasto: " + totalGasto;
 }
}