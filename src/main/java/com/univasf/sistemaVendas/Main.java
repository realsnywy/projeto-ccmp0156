package com.univasf.sistemaVendas;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            CRM crm = new CRM(); // Instância do sistema CRM para gerenciar clientes, produtos e vendas
            int opcao; // Variável para armazenar a opção do menu escolhida pelo usuário
            do {
                // Exibe o menu de opções para o usuário
                System.out.println("1. Adicionar Cliente");
                System.out.println("2. Adicionar Produto");
                System.out.println("3. Registrar Venda");
                System.out.println("4. Listar Clientes");
                System.out.println("5. Listar Produtos");
                System.out.println("6. Listar Vendas");
                System.out.println("7. Histórico de Compras do Cliente");
                System.out.println("8. Listar Clientes Mais Lucrativos");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");
                opcao = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha após a entrada numérica

                switch (opcao) {
                    case 1: {
                        // Adicionar um novo cliente
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Nome: ");
                        String nome = scanner.nextLine();

                        System.out.print("Email: ");
                        String email = scanner.nextLine();

                        System.out.print("Telefone: ");
                        String telefone = scanner.nextLine();

                        crm.adicionarCliente(id, nome, email, telefone); // Chama o método para adicionar cliente
                        break;
                    }
                    case 2: {
                        // Adicionar um novo produto
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Nome do Produto: ");
                        String nome = scanner.nextLine();

                        System.out.print("Preço: ");
                        double preco = scanner.nextDouble();
                        scanner.nextLine();

                        System.out.print("Tipo: ");
                        String tipo = scanner.nextLine();

                        crm.adicionarProduto(id, nome, preco, tipo); // Chama o método para adicionar produto
                        break;
                    }
                    case 3: {
                        // Registrar uma nova venda
                        System.out.print("ID do Cliente: ");
                        int idCliente = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("ID do Produto: ");
                        int idProduto = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Quantidade: ");
                        int qtdProd = scanner.nextInt();
                        scanner.nextLine();

                        Produto produto = crm.buscarProdutoPorId(idProduto); // Busca o produto pelo ID
                        if (produto != null) {
                            // Registra a venda se o produto for encontrado
                            crm.registrarVenda(idCliente, idProduto, qtdProd, produto.getPreco() * qtdProd);
                        } else {
                            System.out.println("Produto não encontrado.");
                        }
                        break;
                    }
                    case 4: {
                        // Listar todos os clientes cadastrados
                        crm.listarClientes();
                        break;
                    }
                    case 5: {
                        // Listar todos os produtos cadastrados
                        crm.listarProdutos();
                        break;
                    }
                    case 6: {
                        // Listar todas as vendas realizadas
                        crm.listarVendas();
                        break;
                    }
                    case 7: {
                        // Exibir o histórico de compras de um cliente específico
                        System.out.print("Informe o ID do Cliente: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();

                        Cliente cliente = crm.buscarClientePorId(id); // Busca o cliente pelo ID
                        if (cliente != null) {
                            cliente.exibirHistoricoCompras(); // Exibe o histórico de compras do cliente
                        } else {
                            System.out.println("Cliente não encontrado.");
                        }
                        break;
                    }
                    case 8: {
                        // Listar os clientes mais lucrativos
                        crm.listarClientesMaisLucrativos();
                        break;
                    }
                    case 0: {
                        // Encerrar o sistema
                        System.out.println("Encerrando o sistema...");
                        break;
                    }
                    default: {
                        // Opção inválida
                        System.out.println("Opção inválida. Tente novamente.");
                    }
                }
            } while (opcao != 0); // Continua exibindo o menu até que a opção 0 seja escolhida
        }
    }
}