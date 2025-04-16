package com.univasf.sistemaVendas;

import java.util.Scanner;

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
                        scanner.nextLine();
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

                        scanner.nextLine();

                        System.out.print("Nome do Produto: ");
                        String nome = scanner.nextLine();

                        System.out.print("Preço: ");
                        double preco = scanner.nextDouble();
                        scanner.nextLine();

                        System.out.print("Tipo: ");
                        String tipo = scanner.nextLine();

                        crm.adicionarProduto(id, nome, preco, tipo);
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