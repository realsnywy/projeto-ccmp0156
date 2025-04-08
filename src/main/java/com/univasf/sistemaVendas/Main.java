package com.univasf.sistemaVendas;

import java.io.*;
import java.util.*;
import java.time.LocalDate;

class Produto {
    int id;
    String nome;
    double preco;

    public Produto(int id, String nome, double preco) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
    }

    @Override
    public String toString() {
        return id + ", " + nome + ", Preço: " + preco;
    }
}

class CRM {
    private static final String CLIENTES_FILE = "clientes.txt";
    private static final String PRODUTOS_FILE = "produtos.txt";
    private static final String VENDAS_FILE = "vendas.txt";
    private List<Cliente> clientes = new ArrayList<>();
    private List<Produto> produtos = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();

    public CRM() {
        carregarClientes();
        carregarProdutos();
        carregarVendas();
    } 

    public Produto buscarProdutoPorId(int id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUTOS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] dados = line.split(", ");
                if (dados.length == 3 && Integer.parseInt(dados[0]) == id) {
                    return new Produto(Integer.parseInt(dados[0]), dados[1], Double.parseDouble(dados[2]));
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

    public void adicionarProduto(int id, String nome, double preco) {
        produtos.add(new Produto(id, nome, preco));
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
                if (dados.length == 3) {
                    produtos.add(new Produto(Integer.parseInt(dados[0]), dados[1], Double.parseDouble(dados[2])));
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
                writer.write(p.id + ", " + p.nome + ", " + p.preco);
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
}

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            CRM crm = new CRM();
            int opcao;
            do {
                System.out.println("1. Adicionar Cliente");
                System.out.println("2. Adicionar Produto");
                System.out.println("3. Registrar Venda");
                System.out.println("4. Listar Clientes");
                System.out.println("5. Listar Produtos");
                System.out.println("6. Listar Vendas");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");
                opcao = scanner.nextInt();
                scanner.nextLine();
                switch (opcao) {
                    case 1: {
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        System.out.print("Nome: ");
                        String nome = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Telefone: ");
                        String telefone = scanner.nextLine();
                        crm.adicionarCliente(id, nome, email, telefone);
                        break;
                    }
                    case 2: {
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        System.out.print("Nome do Produto: ");
                        String nome = scanner.nextLine();
                        System.out.print("Preço: ");
                        double preco = scanner.nextDouble();
                        crm.adicionarProduto(id, nome, preco);
                        break;
                    }
                    case 3: {
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        System.out.print("ID Produto: ");
                        int idProduto = scanner.nextInt();
                        System.out.print("QTD Produto: ");
                        int qtdProd = scanner.nextInt();
                        Produto produto = crm.buscarProdutoPorId(idProduto);
                        crm.registrarVenda(id, idProduto, qtdProd, produto.preco * qtdProd);
                        break;
                    }
                    case 4: {
                        crm.listarClientes();
                        break;
                    }
                    case 5: {
                        crm.listarProdutos();
                        break;
                    }
                    case 6: {
                        crm.listarVendas();
                        break;
                    }
                }
            } while (opcao != 0);
        }
    }
}