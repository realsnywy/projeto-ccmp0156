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

 @Override
 public String toString() {
     return id + ", " + nome + ", " + email + ", " + telefone + ", Compras: " + compras + ", Total Gasto: " + totalGasto;
 }
}