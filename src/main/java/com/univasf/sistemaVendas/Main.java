package com.univasf.sistemaVendas;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CRM crm = new CRM();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Sistema de CRM e Vendas ---");
            System.out.println("1. Adicionar Cliente");
            System.out.println("2. Adicionar Produto");
            System.out.println("3. Registrar Venda");
            System.out.println("4. Listar Clientes");
            System.out.println("5. Listar Produtos");
            System.out.println("6. Listar Vendas");
            System.out.println("7. Listar Clientes Mais Lucrativos");
            System.out.println("8. Listar Produtos Mais Vendidos");
            System.out.println("9. Listar Períodos com Mais Vendas");
            System.out.println("10. Prever Demanda Futura");
            System.out.println("11. Ver Histórico de Compras do Cliente");
            System.out.println("12. Gerar Sugestões para Clientes");
            System.out.println("13. Cadastrar Plano de Assinatura");
            System.out.println("14. Listar Planos de Assinatura");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = -1;
            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Consumir a nova linha
            } else {
                System.out.println("Opção inválida. Por favor, insira um número.");
                scanner.nextLine(); // Limpar o buffer do scanner
                continue;
            }

            switch (opcao) {
                case 1: {
                    System.out.println("\n--- Adicionar Cliente ---");
                    System.out.print("Nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Telefone: ");
                    String telefone = scanner.nextLine();
                    crm.adicionarCliente(nome, email, telefone);
                    break;
                }
                case 2: {
                    System.out.println("\n--- Adicionar Produto ---");
                    System.out.print("Nome do Produto: ");
                    String nome = scanner.nextLine();
                    double preco = 0;
                    while (true) {
                        try {
                            System.out.print("Preço: ");
                            preco = Double.parseDouble(scanner.nextLine());
                            if (preco < 0) {
                                System.out.println("O preço não pode ser negativo.");
                                continue;
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Preço inválido. Use o formato numérico (ex: 29.99).");
                        }
                    }
                    System.out.print("Tipo/Descrição: ");
                    String tipo = scanner.nextLine();
                    crm.adicionarProduto(nome, preco, tipo);
                    break;
                }
                case 3: {
                    System.out.println("\n--- Registrar Venda ---");
                    int idCliente = 0, idProduto = 0, qtdProd = 0;
                    try {
                        System.out.print("ID do Cliente: ");
                        idCliente = Integer.parseInt(scanner.nextLine());
                        System.out.print("ID do Produto: ");
                        idProduto = Integer.parseInt(scanner.nextLine());
                        System.out.print("Quantidade: ");
                        qtdProd = Integer.parseInt(scanner.nextLine());
                        // O parâmetro 'valor' foi removido da chamada de registrarVenda,
                        // pois o valor total é calculado com base no preço do produto e quantidade.
                        crm.registrarVenda(idCliente, idProduto, qtdProd, 0); // O último 0 é um placeholder, não usado.
                    } catch (NumberFormatException e) {
                        System.out.println("ID ou quantidade inválida. Por favor, insira números inteiros.");
                    }
                    break;
                }
                case 4:
                    crm.listarClientes();
                    break;
                case 5:
                    crm.listarProdutos();
                    break;
                case 6:
                    crm.listarVendas();
                    break;
                case 7:
                    crm.listarClientesMaisLucrativos();
                    break;
                case 8:
                    crm.listarProdutosMaisVendidos();
                    break;
                case 9:
                    crm.listarPeriodosMaisVendidos();
                    break;
                case 10:
                    crm.preverDemandaFutura();
                    break;
                case 11: {
                    System.out.println("\n--- Histórico de Compras do Cliente ---");
                    System.out.print("ID do Cliente: ");
                    try {
                        int idCli = Integer.parseInt(scanner.nextLine());
                        Cliente cliente = crm.buscarClientePorId(idCli);
                        if (cliente != null) {
                            cliente.exibirHistoricoCompras();
                        } else {
                            System.out.println("Cliente não encontrado.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID inválido. Por favor, insira um número inteiro.");
                    }
                    break;
                }
                case 12:
                    System.out.println("\n--- Gerando Sugestões para Clientes (Inativos > 7 dias) ---");
                    crm.buscarSugestoesBaseadasNoHistorico();
                    break;
                case 13: { // Cadastrar Plano de Assinatura
                    System.out.println("\n--- Cadastrar Novo Plano de Assinatura ---");
                    System.out.print("Nome do Plano: ");
                    String nomePlano = scanner.nextLine();
                    double precoPlano = 0;
                    while (true) {
                        try {
                            System.out.print("Preço do Plano: ");
                            precoPlano = Double.parseDouble(scanner.nextLine());
                            if (precoPlano < 0) {
                                System.out.println("O preço não pode ser negativo.");
                                continue;
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Preço inválido. Use o formato numérico (ex: 49.90).");
                        }
                    }
                    System.out.print("Duração do Plano (Ex: Mensal, Anual): ");
                    String duracaoPlano = scanner.nextLine();
                    crm.cadastrarPlanoDeAssinatura(nomePlano, precoPlano, duracaoPlano);
                    break;
                }
                case 14: // Listar Planos de Assinatura
                    crm.listarPlanosDeAssinatura();
                    break;
                case 0:
                    System.out.println("Saindo do sistema...");
                    scanner.close();
                    // É uma boa prática fechar outros recursos aqui se necessário, como o Timer do
                    // CRM.
                    // crm.shutdown(); // Você precisaria implementar um método shutdown no CRM para
                    // cancelar o Timer.
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }
}
