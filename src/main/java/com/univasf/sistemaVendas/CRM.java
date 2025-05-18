package com.univasf.sistemaVendas;

import java.sql.Statement;
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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

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
    public void adicionarCliente(String nome, String email, String telefone) {
        Cliente novoCliente = null;

        String sql = "INSERT INTO Cliente (nome, email, telefone, data_cadastro) VALUES (?, ?, ?, CURDATE())";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, telefone);
            stmt.executeUpdate();

            // Recupera o ID gerado automaticamente
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

    // Adiciona um novo produto ao sistema e ao banco de dados
    public void adicionarProduto(String nome, double preco, String tipo) {
        Produto novoProduto = new Produto(nome, preco, tipo);
        produtos.add(novoProduto);

        // Agora o ID é gerado automaticamente pelo banco
        String sql = "INSERT INTO Produtos (nome, descricao, preco, estoque, data_cadastro) VALUES (?, ?, ?, 0, CURDATE())";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nome);
            stmt.setString(2, tipo);
            stmt.setDouble(3, preco);
            stmt.executeUpdate();

            // Recupera o ID gerado automaticamente, se quiser armazenar no objeto
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                novoProduto.setId(generatedKeys.getInt(1));
            }

            System.out.println("Produto adicionado com sucesso.");
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
            cliente.adicionarGasto(venda.getValorTotal());

            // Atualiza o total gasto do cliente no banco de dados
            String sqlAtualizarCliente = "UPDATE Cliente SET total_gasto = total_gasto + ? WHERE id = ?";
            try (Connection conn = ConexaoDB.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlAtualizarCliente)) {
                stmt.setDouble(1, venda.getValorTotal()); // Atualiza o total gasto com o valor da venda
                stmt.setInt(2, clienteId); // Define o ID do cliente
                int linhasAfetadas = stmt.executeUpdate(); // Executa a atualização

                if (linhasAfetadas > 0) {
                    System.out.println("Total gasto do cliente atualizado com sucesso!");
                } else {
                    System.out.println("Erro ao atualizar o total gasto do cliente.");
                }
            } catch (SQLException e) {
                System.out.println("Erro ao registrar venda no cliente: " + e.getMessage());
            }

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
        String sql = """
                SELECT
                    c.id, c.nome, c.email, c.telefone,
                    COUNT(v.id) AS total_compras,  -- Conta quantas vendas o cliente tem
                    SUM(v.valor_total) AS total_gasto
                FROM Cliente c
                LEFT JOIN Vendas v ON c.id = v.cliente_id  -- LEFT JOIN para incluir clientes sem vendas
                GROUP BY c.id
                """;

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            System.out.println("--- Listar Clientes ---");
            while (rs.next()) {
                System.out.printf(
                        "%d, %s, %s, %s, Compras: %d, Total Gasto: %.2f\n",
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getInt("total_compras"),
                        rs.getDouble("total_gasto"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar clientes: " + e.getMessage());
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

    // Vai me dar uma lista de clientes
    public void carregarClientesDoBanco() {
        clientes.clear(); // limpa a lista atual

        String sql = "SELECT id, nome, email, telefone, compras, total_gasto FROM Cliente";
        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

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
        } catch (SQLException e) {
            System.out.println("Erro ao carregar clientes: " + e.getMessage());
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

    public void listarProdutosMaisVendidos() {
        String sql = """
                    SELECT p.id, p.nome, SUM(iv.quantidade) AS total_vendido
                    FROM Produtos p
                    JOIN ItensVenda iv ON p.id = iv.produto_id
                    GROUP BY p.id, p.nome
                    ORDER BY total_vendido DESC
                """;

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nPRODUTOS MAIS VENDIDOS:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                int totalVendido = rs.getInt("total_vendido");

                System.out.printf("ID: %d, Produto: %s, Quantidade Vendida: %d%n", id, nome, totalVendido);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar produtos mais vendidos: " + e.getMessage());
        }
    }

    public void listarPeriodosMaisVendidos() {
        String sql = "SELECT DATE_FORMAT(data_venda, '%Y-%m') AS mes, COUNT(*) AS total_vendas " +
                "FROM Vendas " +
                "GROUP BY mes " +
                "ORDER BY total_vendas DESC";

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nPERÍODOS COM MAIS VENDAS (POR MÊS):");
            while (rs.next()) {
                String mes = rs.getString("mes");
                int total = rs.getInt("total_vendas");
                System.out.printf("Mês: %s, Total de Vendas: %d%n", mes, total);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar períodos mais vendidos: " + e.getMessage());
        }
    }

    public void preverDemandaFutura() {
        String sql = """
                    SELECT
                        p.id AS produto_id,
                        p.nome AS produto_nome,
                        p.descricao AS produto_tipo,
                        SUM(iv.quantidade) AS quantidade_total_geral,
                        AVG(iv.quantidade) AS media_vendas_por_transacao,
                        COUNT(DISTINCT v.id) AS numero_transacoes
                    FROM ItensVenda iv
                    JOIN Produtos p ON iv.produto_id = p.id
                    JOIN Vendas v ON iv.venda_id = v.id
                    WHERE v.data_venda >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH) -- Considera vendas dos últimos 3 meses
                    GROUP BY produto_id, produto_nome, produto_tipo
                    ORDER BY quantidade_total_geral DESC, media_vendas_por_transacao DESC
                    LIMIT 5
                """;

        System.out.println("\n--- Previsão de Demanda Futura (Top 5 Produtos - Últimos 3 Meses) ---");

        try (Connection conn = ConexaoDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Não há dados de vendas suficientes nos últimos 3 meses para gerar uma previsão.");
                return;
            }

            while (rs.next()) {
                String produtoNome = rs.getString("produto_nome");
                int quantidadeTotal = rs.getInt("quantidade_total_geral");
                double mediaVendas = rs.getDouble("media_vendas_por_transacao");

                System.out.printf(
                        "Produto: %s - Demanda Histórica Alta (Total Vendido: %d, Média por Transação: %.2f)%n",
                        produtoNome, quantidadeTotal, mediaVendas);
            }
            System.out.println("Nota: Esta previsão é baseada no histórico de vendas dos últimos 3 meses.");

        } catch (SQLException e) {
            System.err.println("Erro ao prever demanda futura: " + e.getMessage());
        }
    }

    // Gera um relatório estratégico consolidado e exporta para CSV
    public void gerarRelatorioEstrategico() {
        System.out.println("\n--- RELATÓRIO ESTRATÉGICO CONSOLIDADO (CONSOLE E CSV) ---");
        String csvFilePath = "relatorio_estrategico.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath))) {
            // Garante que os dados locais estejam sincronizados com o banco
            carregarClientes();
            carregarVendas(); // Carrega vendas e atualiza histórico de compras dos clientes
            carregarProdutos();

            // --- Clientes Mais Lucrativos ---
            System.out.println("\nCLIENTES MAIS LUCRATIVOS (Console):");
            List<Cliente> clientesOrdenados = new ArrayList<>(this.clientes);
            clientesOrdenados.sort((c1, c2) -> Double.compare(c2.getTotalGasto(), c1.getTotalGasto()));
            if (clientesOrdenados.isEmpty()) {
                System.out.println("Não há clientes para listar.");
            } else {
                for (Cliente cliente : clientesOrdenados) {
                    System.out.printf("Cliente: %s, Total Gasto: %.2f%n", cliente.getNome(), cliente.getTotalGasto());
                }
            }
            // CSV output for Clientes Mais Lucrativos
            writer.println("CLIENTES MAIS LUCRATIVOS");
            writer.println("Nome Cliente,Total Gasto");
            if (clientesOrdenados.isEmpty()) {
                writer.println("N/A,N/A");
            } else {
                for (Cliente cliente : clientesOrdenados) {
                    writer.printf("\"%s\",%.2f%n", cliente.getNome().replace("\"", "\"\""), cliente.getTotalGasto());
                }
            }
            writer.println();

            // --- Produtos Mais Vendidos ---
            System.out.println("\nPRODUTOS MAIS VENDIDOS (Console):");
            listarProdutosMaisVendidos(); // Console output by calling existing method
            // CSV output for Produtos Mais Vendidos
            writer.println("PRODUTOS MAIS VENDIDOS");
            writer.println("ID Produto,Nome Produto,Tipo,Quantidade Vendida");
            String sqlProdutosCsv = """
                        SELECT p.id, p.nome, p.descricao, COALESCE(SUM(iv.quantidade), 0) AS total_vendido
                        FROM Produtos p
                        LEFT JOIN ItensVenda iv ON p.id = iv.produto_id
                        GROUP BY p.id, p.nome, p.descricao
                        ORDER BY total_vendido DESC, p.nome
                    """;
            try (Connection conn = ConexaoDB.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlProdutosCsv);
                    ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    writer.println("N/A,N/A,N/A,N/A");
                } else {
                    while (rs.next()) {
                        writer.printf("%d,\"%s\",\"%s\",%d%n",
                                rs.getInt("id"),
                                rs.getString("nome").replace("\"", "\"\""),
                                rs.getString("descricao").replace("\"", "\"\""),
                                rs.getInt("total_vendido"));
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao gerar CSV para produtos mais vendidos: " + e.getMessage());
                writer.println("Erro ao gerar dados para produtos mais vendidos," + e.getMessage().replace(",", ";"));
            }
            writer.println();

            // --- Períodos Mais Vendidos ---
            System.out.println("\nPERÍODOS COM MAIS VENDAS (POR MÊS) (Console):");
            listarPeriodosMaisVendidos(); // Console output by calling existing method
            // CSV output for Períodos Mais Vendidos
            writer.println("PERÍODOS COM MAIS VENDAS (MÊS/ANO)");
            writer.println("Mês/Ano,Total de Vendas,Faturamento Total");
            String sqlPeriodosCsv = """
                    SELECT
                        DATE_FORMAT(data_venda, '%Y-%m') AS mes_ano,
                        COUNT(*) AS total_vendas,
                        SUM(valor_total) AS faturamento_total
                    FROM Vendas
                    GROUP BY mes_ano
                    ORDER BY total_vendas DESC, faturamento_total DESC
                     """;
            try (Connection conn = ConexaoDB.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlPeriodosCsv);
                    ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    writer.println("N/A,N/A,N/A");
                } else {
                    while (rs.next()) {
                        writer.printf("%s,%d,%.2f%n",
                                rs.getString("mes_ano"),
                                rs.getInt("total_vendas"),
                                rs.getDouble("faturamento_total"));
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao gerar CSV para períodos mais vendidos: " + e.getMessage());
                writer.println("Erro ao gerar dados para períodos mais vendidos," + e.getMessage().replace(",", ";"));
            }
            writer.println();

            // --- Previsão de Demanda Futura ---
            System.out.println("\n--- Previsão de Demanda Futura (Top 5 Produtos - Últimos 3 Meses) (Console) ---");
            preverDemandaFutura(); // Console output by calling existing method
            // CSV output for Previsão de Demanda Futura
            writer.println("PREVISÃO DE DEMANDA FUTURA (TOP 5 PRODUTOS - ÚLTIMOS 3 MESES)");
            writer.println("Nome Produto,Total Vendido,Média por Transação");
            String sqlDemandaCsv = """
                        SELECT
                            p.nome AS produto_nome,
                            SUM(iv.quantidade) AS quantidade_total_geral,
                            AVG(iv.quantidade) AS media_vendas_por_transacao
                        FROM ItensVenda iv
                        JOIN Produtos p ON iv.produto_id = p.id
                        JOIN Vendas v ON iv.venda_id = v.id
                        WHERE v.data_venda >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
                        GROUP BY p.nome
                        ORDER BY quantidade_total_geral DESC, media_vendas_por_transacao DESC
                        LIMIT 5
                    """;
            try (Connection conn = ConexaoDB.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlDemandaCsv);
                    ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    writer.println("Não há dados suficientes para previsão,N/A,N/A");
                } else {
                    while (rs.next()) {
                        writer.printf("\"%s\",%d,%.2f%n",
                                rs.getString("produto_nome").replace("\"", "\"\""),
                                rs.getInt("quantidade_total_geral"),
                                rs.getDouble("media_vendas_por_transacao"));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erro ao gerar CSV para previsão de demanda: " + e.getMessage());
                writer.println("Erro ao gerar dados para previsão de demanda," + e.getMessage().replace(",", ";"));
            }

            System.out.println("\nRelatório estratégico exportado para " + csvFilePath);

        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo CSV '" + csvFilePath + "': " + e.getMessage());
        }
        System.out.println("\n--- FIM DO RELATÓRIO ESTRATÉGICO ---");
    }

    /**
     * Cadastra ou atualiza o plano de assinatura do software CRM.
     *
     * @param tipoPlano     Tipo do plano ("Premium", "Gratuito").
     * @param chaveAtivacao Chave de ativação para planos Premium.
     * @return true se o plano foi alterado/registrado com sucesso, false caso
     *         contrário.
     */
    public boolean cadastrarPlanoAssinaturaCRM(String tipoPlano, String chaveAtivacao) {
        System.out.println("\n--- Cadastro/Atualização do Plano de Assinatura do Software CRM ---");

        if ("Premium".equalsIgnoreCase(tipoPlano)) {
            if (chaveAtivacao != null && !chaveAtivacao.trim().isEmpty()) {
                System.out.println("Chave de ativação fornecida para o plano Premium: " + chaveAtivacao);
                System.out.println("Simulando validação da chave...");
                if (isChavePremiumValida(chaveAtivacao)) {
                    System.out.println("Chave de ativação válida. Plano Premium do CRM ativado (simulado).");
                    System.out.println("Em uma implementação real, o status Premium do CRM seria persistido.");
                    return true;
                } else {
                    System.out.println("Chave de ativação inválida para o plano Premium.");
                    return false;
                }
            } else {
                System.out.println("Ativação do plano Premium requer uma chave de ativação.");
                return false;
            }
        } else if ("Gratuito".equalsIgnoreCase(tipoPlano)) {
            System.out.println("Plano do CRM configurado para Gratuito (simulado).");
            System.out.println("Em uma implementação real, o status Gratuito do CRM seria persistido.");
            return true;
        } else {
            System.out.println("Tipo de plano desconhecido fornecido para o CRM: " + tipoPlano);
            return false;
        }
    }

    // Simula a validação da chave Premium.
    private boolean isChavePremiumValida(String chaveAtivacao) {
        return "PREMIUM_KEY_123".equals(chaveAtivacao) || "XYZ_VALID_CRM_KEY".equals(chaveAtivacao);
    }
}
