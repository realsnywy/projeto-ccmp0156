package com.univasf.sistemaVendas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CRM {
    private List<Cliente> clientes = new ArrayList<>();
    private List<Produto> produtos = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();
    private List<PlanoDeAssinatura> planosDeAssinatura = new ArrayList<>();

    private Timer sugestoesTimer = new Timer();

    public CRM() {
        carregarClientes();
        carregarProdutos();
        carregarVendas();
        carregarPlanosDeAssinatura();

        sugestoesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                buscarSugestoesBaseadasNoHistorico();
            }
        }, 0, 24 * 60 * 60 * 1000);
    }

    public void cadastrarPlanoDeAssinatura(String nome, double preco, String duracao) {
        PlanoDeAssinatura novoPlano = new PlanoDeAssinatura(nome, preco, duracao);
        String sql = "INSERT INTO PlanosDeAssinatura (nome, preco, duracao) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nome);
            stmt.setDouble(2, preco);
            stmt.setString(3, duracao);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        novoPlano.setId(generatedKeys.getInt(1));
                        planosDeAssinatura.add(novoPlano);
                        System.out.println("Plano de Assinatura cadastrado com sucesso: " + novoPlano.getNome()
                                + " (ID: " + novoPlano.getId() + ")");
                    }
                }
            } else {
                System.out.println("Nenhuma linha afetada ao cadastrar o plano de assinatura.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar Plano de Assinatura: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarPlanosDeAssinatura() {
        String sql = "SELECT id, nome, preco, duracao FROM PlanosDeAssinatura";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PlanoDeAssinatura plano = new PlanoDeAssinatura(
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getString("duracao"));
                plano.setId(rs.getInt("id"));
                planosDeAssinatura.add(plano);
            }
            System.out.println(planosDeAssinatura.size() + " planos de assinatura carregados.");
        } catch (SQLException e) {
            System.out.println("Erro ao carregar planos de assinatura: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void listarPlanosDeAssinatura() {
        if (planosDeAssinatura.isEmpty()) {
            System.out.println("Nenhum plano de assinatura cadastrado.");
            return;
        }
        System.out.println("\n--- Planos de Assinatura Disponíveis ---");
        for (PlanoDeAssinatura plano : planosDeAssinatura) {
            System.out.println(plano);
        }
    }

    public Cliente buscarClientePorId(int clienteId) {
        return clientes.stream()
                .filter(c -> c.getId() == clienteId)
                .findFirst()
                .orElse(null);
    }

    public Produto buscarProdutoPorId(int id) {
        return produtos.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void adicionarCliente(String nome, String email, String telefone) {
        Cliente novoCliente = null;
        String sql = "INSERT INTO Cliente (nome, email, telefone, data_cadastro) VALUES (?, ?, ?, CURDATE())";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, telefone);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                novoCliente = new Cliente(idGerado, nome, email, telefone);
                clientes.add(novoCliente);
                System.out.println("Cliente adicionado com ID: " + idGerado);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar cliente: " + e.getMessage());
        }
    }

    public void adicionarProduto(String nome, double preco, String tipo) {
        Produto novoProduto = new Produto(nome, preco, tipo);
        String sql = "INSERT INTO Produtos (nome, descricao, preco, estoque, data_cadastro) VALUES (?, ?, ?, 0, CURDATE())";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nome);
            stmt.setString(2, tipo);
            stmt.setDouble(3, preco);
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                novoProduto.setId(generatedKeys.getInt(1));
                produtos.add(novoProduto);
                System.out.println("Produto adicionado com sucesso com ID: " + novoProduto.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    public void registrarVenda(int clienteId, int produtoId, int qtdProd, double valor) {
        Cliente cliente = buscarClientePorId(clienteId);
        Produto produto = buscarProdutoPorId(produtoId);

        if (cliente != null && produto != null) {
            if (qtdProd <= 0) {
                System.out.println("Quantidade do produto deve ser positiva.");
                return;
            }
            Venda venda = new Venda(cliente, produto, qtdProd, LocalDate.now());

            String sqlVenda = "INSERT INTO Vendas (cliente_id, data_venda, valor_total) VALUES (?, ?, ?)";
            String sqlItemVenda = "INSERT INTO ItensVenda (venda_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

            Connection conn = null;
            try {
                conn = ConexaoDB.getConnection();
                conn.setAutoCommit(false);

                try (PreparedStatement stmtVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                    stmtVenda.setInt(1, clienteId);
                    stmtVenda.setDate(2, java.sql.Date.valueOf(venda.getData()));
                    stmtVenda.setDouble(3, venda.getValorTotal());
                    stmtVenda.executeUpdate();

                    int vendaId;
                    try (ResultSet generatedKeys = stmtVenda.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            vendaId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Falha ao obter o ID da venda, nenhum ID retornado.");
                        }
                    }

                    try (PreparedStatement stmtItemVenda = conn.prepareStatement(sqlItemVenda)) {
                        stmtItemVenda.setInt(1, vendaId);
                        stmtItemVenda.setInt(2, produtoId);
                        stmtItemVenda.setInt(3, qtdProd);
                        stmtItemVenda.setDouble(4, produto.getPreco());
                        stmtItemVenda.executeUpdate();
                    }
                }

                String sqlAtualizarCliente = "UPDATE Cliente SET total_gasto = total_gasto + ?, compras = compras + 1 WHERE id = ?";
                try (PreparedStatement stmtAtualizaCliente = conn.prepareStatement(sqlAtualizarCliente)) {
                    stmtAtualizaCliente.setDouble(1, venda.getValorTotal());
                    stmtAtualizaCliente.setInt(2, clienteId);
                    int linhasAfetadas = stmtAtualizaCliente.executeUpdate();
                    if (linhasAfetadas == 0) {
                        throw new SQLException("Falha ao atualizar dados do cliente.");
                    }
                }

                conn.commit();
                vendas.add(venda);
                cliente.adicionarGasto(venda.getValorTotal());
                cliente.registrarCompra(venda);
                System.out.println("Venda registrada com sucesso!");

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        System.out.println("Erro ao reverter transação: " + ex.getMessage());
                    }
                }
                System.out.println("Erro ao registrar venda: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        System.out.println("Erro ao fechar conexão: " + e.getMessage());
                    }
                }
            }
        } else {
            if (cliente == null)
                System.out.println("Cliente com ID " + clienteId + " não encontrado.");
            if (produto == null)
                System.out.println("Produto com ID " + produtoId + " não encontrado.");
        }
    }

    public void listarClientes() {
        String sql = """
                SELECT
                    c.id, c.nome, c.email, c.telefone, c.compras, c.total_gasto
                FROM Cliente c
                ORDER BY c.nome
                """;

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- Lista de Clientes ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf(
                        "ID: %d, Nome: %s, Email: %s, Telefone: %s, Compras: %d, Total Gasto: R$ %.2f%n",
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getInt("compras"),
                        rs.getDouble("total_gasto"));
            }
            if (!found) {
                System.out.println("Nenhum cliente cadastrado.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void listarProdutos() {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto para listar. Carregando do banco...");
            carregarProdutos();
        }
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        System.out.println("\n--- Lista de Produtos ---");
        for (Produto p : produtos) {
            System.out.println(p);
        }
    }

    public void listarVendas() {
        if (vendas.isEmpty()) {
            System.out.println("Nenhuma venda para listar. Carregando do banco...");
        }
        if (vendas.isEmpty()) {
            System.out.println("Nenhuma venda registrada.");
            return;
        }
        System.out.println("\n--- Lista de Vendas ---");
        for (Venda v : vendas) {
            System.out.println(v);
        }
    }

    private void carregarClientes() {
        String sql = "SELECT id, nome, email, telefone, compras, total_gasto FROM Cliente";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            clientes.clear();
            while (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getInt("compras"),
                        rs.getDouble("total_gasto"));
                clientes.add(cliente);
            }
            System.out.println(clientes.size() + " clientes carregados.");
        } catch (SQLException e) {
            System.out.println("Erro ao carregar clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarProdutos() {
        String sql = "SELECT id, nome, descricao, preco FROM Produtos";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            produtos.clear();
            while (rs.next()) {
                Produto produto = new Produto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getString("descricao"));
                produtos.add(produto);
            }
            System.out.println(produtos.size() + " produtos carregados.");
        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarVendas() {
        String sql = """
                    SELECT v.id AS venda_id, v.cliente_id, v.data_venda, v.valor_total,
                           iv.produto_id, iv.quantidade, iv.preco_unitario,
                           p.nome as produto_nome, p.descricao as produto_descricao
                    FROM Vendas v
                    JOIN ItensVenda iv ON v.id = iv.venda_id
                    JOIN Produtos p ON iv.produto_id = p.id
                    ORDER BY v.data_venda DESC
                """;
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            vendas.clear();
            while (rs.next()) {
                Cliente cliente = buscarClientePorId(rs.getInt("cliente_id"));
                Produto produto = new Produto(
                        rs.getInt("produto_id"),
                        rs.getString("produto_nome"),
                        rs.getDouble("preco_unitario"),
                        rs.getString("produto_descricao"));

                if (cliente != null) {
                    Venda venda = new Venda(
                            cliente,
                            produto,
                            rs.getInt("quantidade"),
                            rs.getDate("data_venda").toLocalDate());
                    vendas.add(venda);
                }
            }
            System.out.println(vendas.size() + " vendas carregadas.");
        } catch (SQLException e) {
            System.out.println("Erro ao carregar vendas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void carregarClientesDoBanco() {
        carregarClientes();
    }

    public void listarClientesMaisLucrativos() {
        if (clientes.isEmpty())
            carregarClientes();
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente para listar.");
            return;
        }
        List<Cliente> clientesOrdenados = new ArrayList<>(clientes);
        clientesOrdenados.sort((c1, c2) -> Double.compare(c2.getTotalGasto(), c1.getTotalGasto()));
        System.out.println("\n--- Clientes Mais Lucrativos ---");
        for (Cliente cliente : clientesOrdenados) {
            System.out.printf("Cliente: %s, Total Gasto: R$ %.2f%n", cliente.getNome(), cliente.getTotalGasto());
        }
    }

    public void buscarSugestoesBaseadasNoHistorico() {
        LocalDate hoje = LocalDate.now();
        if (clientes.isEmpty())
            carregarClientes();
        if (produtos.isEmpty())
            carregarProdutos();

        for (Cliente cliente : clientes) {
            LocalDate ultimaCompraData = null;
            if (cliente.getHistoricoCompras() != null && !cliente.getHistoricoCompras().isEmpty()) {
                ultimaCompraData = cliente.getHistoricoCompras().stream()
                        .map(Venda::getData)
                        .max(LocalDate::compareTo)
                        .orElse(null);
            }

            if (ultimaCompraData == null) {
                ultimaCompraData = hoje.minusDays(31);
            }

            long diasInativo = ChronoUnit.DAYS.between(ultimaCompraData, hoje);

            if (diasInativo > 7) {
                try {
                    List<Integer> sugestoesIds = cliente.sugerirProdutosSimilares(this.produtos);
                    List<Produto> produtosSugeridos = new ArrayList<>();
                    for (Integer id : sugestoesIds) {
                        Produto p = buscarProdutoPorId(id);
                        if (p != null)
                            produtosSugeridos.add(p);
                    }
                    enviarSugestoesParaCliente(cliente, produtosSugeridos);
                } catch (Exception e) {
                    System.out.println(
                            "Erro ao gerar sugestões para o cliente " + cliente.getNome() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void enviarSugestoesParaCliente(Cliente cliente, List<Produto> sugestoes) {
        if (sugestoes == null || sugestoes.isEmpty()) {
            return;
        }

        System.out.println(
                "\nEnviando sugestões para o cliente " + cliente.getNome() + " (ID: " + cliente.getId() + "):");
        for (Produto produto : sugestoes) {
            System.out.println("- " + produto.getNome() + " - Preço: R$ " + produto.getPreco());
        }
    }

    public void listarProdutosMaisVendidos() {
        String sql = """
                    SELECT p.id, p.nome, SUM(iv.quantidade) AS total_vendido
                    FROM Produtos p
                    JOIN ItensVenda iv ON p.id = iv.produto_id
                    GROUP BY p.id, p.nome
                    ORDER BY total_vendido DESC
                    LIMIT 10
                """;

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- Produtos Mais Vendidos ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                int totalVendido = rs.getInt("total_vendido");
                System.out.printf("ID: %d, Produto: %s, Quantidade Vendida: %d%n", id, nome, totalVendido);
            }
            if (!found)
                System.out.println("Não há dados de vendas suficientes para listar os produtos mais vendidos.");

        } catch (SQLException e) {
            System.out.println("Erro ao listar produtos mais vendidos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void listarPeriodosMaisVendidos() {
        String sql = """
                SELECT DATE_FORMAT(data_venda, '%Y-%m') AS mes_venda, COUNT(*) AS total_vendas, SUM(valor_total) as faturamento_mes
                FROM Vendas
                GROUP BY mes_venda
                ORDER BY mes_venda DESC
                """;

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- Períodos com Mais Vendas (por Mês) ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String mes = rs.getString("mes_venda");
                int total = rs.getInt("total_vendas");
                double faturamento = rs.getDouble("faturamento_mes");
                System.out.printf("Mês: %s, Total de Vendas: %d, Faturamento: R$ %.2f%n", mes, total, faturamento);
            }
            if (!found)
                System.out.println("Não há dados de vendas para listar os períodos.");

        } catch (SQLException e) {
            System.out.println("Erro ao listar períodos mais vendidos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void preverDemandaFutura() {
        System.out.println("\n--- Previsão de Demanda Futura (Baseada nos Top 5 Produtos Históricos) ---");
        listarProdutosMaisVendidos();
        System.out.println("Nota: Esta previsão é baseada no histórico geral de vendas dos produtos mais vendidos.");
    }
}
