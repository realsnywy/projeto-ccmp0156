package com.univasf.sistemaVendas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cliente {
    private int id;
    private String nome;
    private String email;
    private String telefone;
    private int compras;
    private double totalGasto;
    private List<Venda> historicoCompras;

    // Construtor para inicializar os atributos do cliente
    public Cliente(int id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.compras = 0; // Inicializa o número de compras como 0
        this.totalGasto = 0.0; // Inicializa o total gasto como 0.0
    }

    // Método para registrar uma compra no banco de dados e atualizar os dados do
    // cliente
    public void registrarCompra(Venda venda) {
        String sqlAtualizarCliente = "UPDATE Cliente SET compras = compras + 1, total_gasto = total_gasto + ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlAtualizarCliente)) {
            stmt.setDouble(1, venda.valor); // Define o valor da venda
            stmt.setInt(2, this.id); // Define o ID do cliente
            stmt.executeUpdate(); // Executa a atualização no banco de dados
            System.out.println("Venda registrada para o cliente com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao registrar venda no cliente: " + e.getMessage());
        }
    }

    // Método para obter os produtos mais comprados pelo cliente
    public Map<Integer, Integer> getProdutosMaisComprados() {
        String sql = """
                    SELECT iv.produto_id, SUM(iv.quantidade) AS total_comprado
                    FROM ItensVenda iv
                    JOIN Vendas v ON iv.venda_id = v.id
                    WHERE v.cliente_id = ?
                    GROUP BY iv.produto_id
                """;

        Map<Integer, Integer> produtosQuantidade = new HashMap<>();
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id); // Define o ID do cliente
            ResultSet rs = stmt.executeQuery(); // Executa a consulta
            while (rs.next()) {
                // Adiciona o produto e a quantidade comprada ao mapa
                produtosQuantidade.put(rs.getInt("produto_id"), rs.getInt("total_comprado"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter produtos mais comprados: " + e.getMessage());
        }
        return produtosQuantidade; // Retorna o mapa de produtos e suas quantidades
    }

    // Método para sugerir produtos similares com base no histórico de compras
    public List<Integer> sugerirProdutosSimilares(List<Produto> todosProdutos) {
        // Obter o produto mais comprado
        Map<Integer, Integer> produtosComprados = getProdutosMaisComprados();
        if (produtosComprados.isEmpty()) {
            // Caso o cliente não tenha histórico de compras, sugere os 3 produtos mais
            // caros
            return todosProdutos.stream()
                    .sorted((p1, p2) -> Double.compare(p2.preco, p1.preco))
                    .limit(3)
                    .map(p -> p.id)
                    .toList();
        }

        // Obtém o ID do produto mais comprado
        int produtoMaisComprado = produtosComprados.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();

        // Filtra e sugere os 3 produtos mais caros, excluindo o mais comprado
        return todosProdutos.stream()
                .filter(p -> p.id != produtoMaisComprado)
                .sorted((p1, p2) -> Double.compare(p2.preco, p1.preco))
                .limit(3)
                .map(p -> p.id)
                .toList();
    }

    // Método para exibir o histórico de compras do cliente
    public void exibirHistoricoCompras() {
        String sql = """
                    SELECT v.id AS venda_id, v.data_venda, v.valor_total
                    FROM Vendas v
                    WHERE v.cliente_id = ?
                """;

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id); // Define o ID do cliente
            ResultSet rs = stmt.executeQuery(); // Executa a consulta
            System.out.println("Histórico de compras do cliente " + this.nome + ":");
            while (rs.next()) {
                // Exibe os detalhes de cada venda
                System.out.printf("Venda ID: %d, Data: %s, Valor Total: %.2f%n",
                        rs.getInt("venda_id"),
                        rs.getTimestamp("data_venda"),
                        rs.getDouble("valor_total"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar histórico de compras: " + e.getMessage());
        }
    }

    // Getters e Setters para os atributos do cliente
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

    public void setHistoricoCompras(List<Venda> historicoCompras) {
        this.historicoCompras = historicoCompras;
    }

    // Método para exibir os dados do cliente como uma string
    @Override
    public String toString() {
        return id + ", " + nome + ", " + email + ", " + telefone + ", Compras: " + compras + ", Total Gasto: "
                + totalGasto;
    }
}