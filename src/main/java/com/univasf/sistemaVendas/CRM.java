package com.univasf.sistemaVendas;

import java.io.*; // Seria bom melhorar isso mais tarde, já que o uso de * é considerado uma prática ruim.
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class CRM {
    private static final String CLIENTES_FILE = "clientes.txt";
    private static final String PRODUTOS_FILE = "produtos.txt";
    private static final String VENDAS_FILE = "vendas.txt";
    private List<Cliente> clientes = new ArrayList<>();
    private List<Produto> produtos = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();

    private Timer sugestoesTimer = new Timer();

    public CRM() {
        carregarClientes();
        carregarProdutos();
        carregarVendas();

        // Iniciar o timer para sugestões a cada 24 horas
        sugestoesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                buscarSugestoesBaseadasNoHistorico();
            }
        }, 0, 24 * 60 * 60 * 1000); // Executa uma vez por dia
    }

    public Produto buscarProdutoPorId(int id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUTOS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] dados = line.split(", ");
                if (dados.length == 4 && Integer.parseInt(dados[0]) == id) {
                    return new Produto(Integer.parseInt(dados[0]), dados[1], Double.parseDouble(dados[2]), dados[3]);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao buscar produto: " + e.getMessage());
        }
        return null; // Retorna null se o produto não for encontrado
    }

    public void adicionarCliente(int id, String nome, String email, String telefone) {
        clientes.add(new Cliente(id, nome, email, telefone));
        salvarClientes();
    }

    public void adicionarProduto(int id, String nome, double preco, String tipo) {
        produtos.add(new Produto(id, nome, preco, tipo));
        salvarProdutos();
    }

    public void registrarVenda(int clienteId, int produtoId, int qtdProd, double valor) {
        Cliente cliente = clientes.stream().filter(c -> c.id == clienteId).findFirst().orElse(null);
        Produto produto = produtos.stream().filter(p -> p.id == produtoId).findFirst().orElse(null);

        if (cliente != null && produto != null) {
            Venda venda = new Venda(cliente, produto, qtdProd, LocalDate.now());
            vendas.add(venda);
            cliente.registrarCompra(venda); // Registra a venda no cliente
        } else {
            System.out.println("Cliente ou Produto não encontrado.");
        }
        salvarVendas();
    }

    public void listarClientes() {
        for (Cliente c : clientes) {
            System.out.println(c);
        }
    }

    public void listarProdutos() {
        for (Produto p : produtos) {
            System.out.println(p);
        }
    }

    public void listarVendas() {
        for (Venda v : vendas) {
            System.out.println(v);
        }
    }

    private void carregarClientes() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CLIENTES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] dados = line.split(", ");
                if (dados.length >= 4) {
                    clientes.add(new Cliente(Integer.parseInt(dados[0]), dados[1], dados[2], dados[3]));
                }
            }
        } catch (IOException e) {
            System.out.println("Nenhum cliente cadastrado ainda.");
        }
    }

    private void carregarProdutos() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUTOS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] dados = line.split(", ");
                if (dados.length == 4) {
                    produtos.add(
                            new Produto(Integer.parseInt(dados[0]), dados[1], Double.parseDouble(dados[2]), dados[3]));
                }
            }
        } catch (IOException e) {
            System.out.println("Nenhum produto cadastrado ainda.");
        }
    }

    private void carregarVendas() {
        try (BufferedReader reader = new BufferedReader(new FileReader(VENDAS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] dados = line.split(", ");
                if (dados.length == 5) {
                    Cliente cliente = clientes.stream()
                            .filter(c -> c.id == Integer.parseInt(dados[0]))
                            .findFirst().orElse(null);
                    Produto produto = produtos.stream()
                            .filter(p -> p.id == Integer.parseInt(dados[1]))
                            .findFirst().orElse(null);

                    if (cliente != null && produto != null) {
                        Venda venda = new Venda(cliente, produto,
                                Integer.parseInt(dados[2]),
                                LocalDate.parse(dados[4]));
                        vendas.add(venda);
                        cliente.registrarCompra(venda);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Nenhuma venda registrada ainda.");
        }
    }

    private void salvarClientes() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CLIENTES_FILE))) {
            for (Cliente c : clientes) {
                writer.write(c.id + ", " + c.nome + ", " + c.email + ", " + c.telefone);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar clientes.");
        }
    }

    private void salvarProdutos() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUTOS_FILE))) {
            for (Produto p : produtos) {
                writer.write(p.id + ", " + p.nome + ", " + p.preco + ", " + p.tipo);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar produtos.");
        }
    }

    private void salvarVendas() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VENDAS_FILE))) {
            for (Venda v : vendas) {
                writer.write(v.getCliente().id + ", " + v.getProduto().id + ", " + v.getQuantidade() + ", "
                        + v.getValorTotal() + ", " + v.getData());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar vendas.");
        }
    }

    public void listarClientesMaisLucrativos() {
        if (clientes.isEmpty()) {
            System.out.println("Não há clientes cadastrados.\n");
            return; // deixar o metodo se nao houver clientes
        }

        // Ordena em ordem decrescente por isso c2 primeiro
        List<Cliente> clientesOrdenados = new ArrayList<>(clientes);
        clientesOrdenados.sort((c1, c2) -> Double.compare(c2.totalGasto, c1.totalGasto));

        System.out.println("\n TODOS CLIENTES POR ORDEM DE GASTO: \n");
        for (int i = 0; i < clientesOrdenados.size(); i++) {
            Cliente cliente = clientesOrdenados.get(i);
            System.out.printf("%dº - %s (ID: %d) - Total gasto: R$%f\n", i + 1, cliente.nome, cliente.id,
                    cliente.totalGasto);
        }
    }

    // Método responsável por buscar sugestões baseadas no histórico de compras.
    // NOTA: Este método ficará aguardando a implementação do histórico de compras
    // do Pedro para ser finalizado.
    public void buscarSugestoesBaseadasNoHistorico() {
        LocalDate hoje = LocalDate.now();

        for (Cliente cliente : clientes) {
            long diasInativo = ChronoUnit.DAYS.between(
                    cliente.historicoCompras.isEmpty() ? cliente.getDataCadastro()
                            : cliente.historicoCompras.get(cliente.historicoCompras.size() - 1).getData(),
                    hoje);

            if (diasInativo > 7) {
                List<Produto> sugestoes = cliente.sugerirProdutosSimilares(produtos);
                enviarSugestoesParaCliente(cliente, sugestoes);
            }
        }
    }

    private void enviarSugestoesParaCliente(Cliente cliente, List<Produto> sugestoes) {
        System.out.println("Enviando sugestões para o cliente " + cliente.nome + ":");
        for (Produto produto : sugestoes) {
            System.out.println("- " + produto.nome);
        }
    }
}
