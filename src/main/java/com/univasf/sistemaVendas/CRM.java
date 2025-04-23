package com.univasf.sistemaVendas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class CRM {
    private List<Cliente> clientes = new ArrayList<>();
    private List<Produto> produtos = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();

    private Timer sugestoesTimer = new Timer();

    public CRM() {
        // Carrega os dados iniciais do banco de dados
        carregarClientes();
        carregarProdutos();
        carregarVendas();

        // Inicia o timer para gerar sugestões baseadas no histórico de compras a cada
        // 24 horas
        sugestoesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                buscarSugestoesBaseadasNoHistorico();
            }
        }, 0, 24 * 60 * 60 * 1000); // Executa uma vez por dia
    }

    // Busca um cliente pelo ID
    public Cliente buscarClientePorId(int clienteId) {
        return clientes.stream()
                .filter(c -> c.getId() == clienteId)
                .findFirst()
                .orElse(null);
    }

    // Busca um produto pelo ID
    public Produto buscarProdutoPorId(int id) {
        return produtos.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Adiciona um novo cliente ao sistema e ao banco de dados
    public void adicionarCliente(int id, String nome, String email, String telefone) {
        if (clientes.stream().anyMatch(c -> c.getId() == id)) {
            System.out.println("Já existe um cliente com esse ID.");
            return;
        }
        Cliente novoCliente = new Cliente(id, nome, email, telefone);
        clientes.add(novoCliente);

        // Salva o cliente no banco de dados
        String sql = "INSERT INTO Cliente (id, nome, email, telefone, data_cadastro) VALUES (?, ?, ?, ?, CURDATE())";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, nome);
            stmt.setString(3, email);
            stmt.setString(4, telefone);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar cliente: " + e.getMessage());
        }
    }

    // Adiciona um novo produto ao sistema e ao banco de dados
    public void adicionarProduto(int id, String nome, double preco, String tipo) {
        if (produtos.stream().anyMatch(p -> p.getId() == id)) {
            System.out.println("Já existe um produto com esse ID.");
            return;
        }
        Produto novoProduto = new Produto(id, nome, preco, tipo);
        produtos.add(novoProduto);

        // Salva o produto no banco de dados
        String sql = "INSERT INTO Produtos (id, nome, descricao, preco, estoque, data_cadastro) VALUES (?, ?, ?, ?, 0, CURDATE())";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, nome);
            stmt.setString(3, tipo);
            stmt.setDouble(4, preco);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    // Registra uma nova venda no sistema e no banco de dados
    public void registrarVenda(int clienteId, int produtoId, int qtdProd, double valor) {
        Cliente cliente = buscarClientePorId(clienteId);
        Produto produto = buscarProdutoPorId(produtoId);

        if (cliente != null && produto != null) {
            Venda venda = new Venda(cliente, produto, qtdProd, LocalDate.now());
            vendas.add(venda);
            cliente.registrarCompra(venda);

            // Salva a venda e os itens da venda no banco de dados
            String sqlVenda = "INSERT INTO Vendas (cliente_id, data_venda, valor_total) VALUES (?, ?, ?)";
            String sqlItemVenda = "INSERT INTO ItensVenda (venda_id, produto_id, quantidade, preco_unitario) VALUES (LAST_INSERT_ID(), ?, ?, ?)";

            try (Connection conn = ConexaoDB.getConnection();
                    PreparedStatement stmtVenda = conn.prepareStatement(sqlVenda);
                    PreparedStatement stmtItemVenda = conn.prepareStatement(sqlItemVenda)) {

                // Insere a venda
                stmtVenda.setInt(1, clienteId);
                stmtVenda.setDate(2, java.sql.Date.valueOf(venda.getData()));
                stmtVenda.setDouble(3, venda.getValorTotal());
                stmtVenda.executeUpdate();

                // Insere o item da venda
                stmtItemVenda.setInt(1, produtoId);
                stmtItemVenda.setInt(2, qtdProd);
                stmtItemVenda.setDouble(3, produto.getPreco());
                stmtItemVenda.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Erro ao registrar venda: " + e.getMessage());
            }
        } else {
            System.out.println("Cliente ou Produto não encontrado.");
        }
    }

    // Lista todos os clientes cadastrados
    public void listarClientes() {
        for (Cliente c : clientes) {
            System.out.println(c);
        }
    }

    // Lista todos os produtos cadastrados
    public void listarProdutos() {
        for (Produto p : produtos) {
            System.out.println(p);
        }
    }

    // Lista todas as vendas registradas
    public void listarVendas() {
        for (Venda v : vendas) {
            System.out.println(v);
        }
    }

    // Carrega os clientes do banco de dados para a lista local
    private void carregarClientes() {
        String sql = "SELECT * FROM Cliente";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"));
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar clientes: " + e.getMessage());
        }
    }

    // Carrega os produtos do banco de dados para a lista local
    private void carregarProdutos() {
        String sql = "SELECT * FROM Produtos";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto produto = new Produto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getString("descricao"));
                produtos.add(produto);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    // Carrega as vendas do banco de dados para a lista local
    private void carregarVendas() {
        String sql = """
                    SELECT v.id AS venda_id, v.cliente_id, v.data_venda, v.valor_total,
                           iv.produto_id, iv.quantidade, iv.preco_unitario
                    FROM Vendas v
                    JOIN ItensVenda iv ON v.id = iv.venda_id
                """;
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cliente cliente = buscarClientePorId(rs.getInt("cliente_id"));
                Produto produto = buscarProdutoPorId(rs.getInt("produto_id"));

                if (cliente != null && produto != null) {
                    Venda venda = new Venda(cliente, produto,
                            rs.getInt("quantidade"),
                            rs.getDate("data_venda").toLocalDate());
                    vendas.add(venda);
                    cliente.registrarCompra(venda);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar vendas: " + e.getMessage());
        }
    }

    // Lista os clientes mais lucrativos, ordenados pelo total gasto
    public void listarClientesMaisLucrativos() {
        clientes.sort((c1, c2) -> Double.compare(c2.getTotalGasto(), c1.getTotalGasto()));
        System.out.println("\nCLIENTES MAIS LUCRATIVOS:");
        for (Cliente cliente : clientes) {
            System.out.printf("Cliente: %s, Total Gasto: %.2f%n", cliente.getNome(), cliente.getTotalGasto());
        }
    }

    // Gera sugestões de produtos para clientes inativos
    public void buscarSugestoesBaseadasNoHistorico() {
        LocalDate hoje = LocalDate.now();

        for (Cliente cliente : clientes) {
            // Define a data da última compra ou usa uma data padrão
            LocalDate ultimaCompraData = cliente.getHistoricoCompras().isEmpty()
                    ? hoje.minusDays(30) // Assume que o cliente está inativo há 30 dias se não houver histórico
                    : cliente.getHistoricoCompras().get(cliente.getHistoricoCompras().size() - 1).getData();

            long diasInativo = ChronoUnit.DAYS.between(ultimaCompraData, hoje);

            // Envia sugestões apenas para clientes inativos há mais de 7 dias
            if (diasInativo > 7) {
                try {
                    List<Integer> sugestoes = cliente.sugerirProdutosSimilares(produtos);
                    enviarSugestoesParaCliente(cliente, sugestoes);
                } catch (Exception e) {
                    System.out.println(
                            "Erro ao gerar sugestões para o cliente " + cliente.getNome() + ": " + e.getMessage());
                }
            }
        }
    }

    // Envia sugestões de produtos para um cliente específico
    private void enviarSugestoesParaCliente(Cliente cliente, List<Integer> sugestoes) {
        if (sugestoes == null || sugestoes.isEmpty()) {
            System.out.println("Nenhuma sugestão disponível para o cliente " + cliente.getNome());
            return;
        }

        System.out.println("Enviando sugestões para o cliente " + cliente.getNome() + ":");
        for (Integer produtoId : sugestoes) {
            Produto produto = buscarProdutoPorId(produtoId);
            if (produto != null) {
                System.out.println("- " + produto.getNome() + " - Preço: R$ " + produto.getPreco());
            } else {
                System.out.println("- Produto com ID " + produtoId + " não encontrado.");
            }
        }
    }
}