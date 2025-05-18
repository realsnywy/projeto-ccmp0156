package com.univasf.sistemaVendas;

import java.util.Scanner;

public class Main {

    // Workaround para limpar a tela
    public static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Gerencia ativação/gerenciamento do plano Premium do CRM
    private static boolean gerenciarPlanoAssinaturaCRM(Scanner scanner, boolean currentIsPremium, CRM crm) {
        boolean updatedIsPremium = currentIsPremium;
        if (currentIsPremium) {
            System.out.println("--- Gerenciar Plano Premium do CRM ---");
            System.out.println("Você já está utilizando o plano Premium do CRM.");
            abrirNavegador("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println("Isso não era pra dar errado.");
            }
        } else {
            System.out.println("--- Ativar Plano Premium do CRM ---");
            abrirNavegador("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            System.out.print(
                    "Deseja inserir uma chave de ativação para o plano Premium do CRM para esta sessão? (S/N): ");
            String respostaAtivacao = scanner.nextLine();
            if (respostaAtivacao.equalsIgnoreCase("S")) {
                System.out.print("Digite a chave de ativação Premium: ");
                String chave = scanner.nextLine();
                // Simula ativação do plano Premium
                if (crm.cadastrarPlanoAssinaturaCRM("Premium", chave)) {
                    updatedIsPremium = true;
                    System.out.println("Plano Premium do CRM ativado para esta sessão com a chave fornecida.");
                } else {
                    System.out.println(
                            "Falha ao ativar o plano Premium com a chave fornecida. Permanecendo no plano Gratuito.");
                }
            }
        }
        limparTela();
        return updatedIsPremium;
    }

    // Abre o navegador padrão com a URL fornecida
    private static void abrirNavegador(String url) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                desktop.browse(new java.net.URI(url));
            } else {
                System.out.println("Navegador não suportado.");
            }
        } catch (Exception e) {
            System.out.println("Não foi possível abrir o navegador.");
        }
    }

    public static void main(String[] args) {
        // Executa o script do banco de dados na inicialização
        ConexaoDB.runDatabaseScript();

        boolean isPremiumUser = false; // Controla se o usuário do CRM tem plano Premium

        try (Scanner scanner = new Scanner(System.in)) {
            CRM crm = new CRM(); // Instância do sistema CRM
            limparTela();
            System.out.println("Bem-vindo ao Sistema CRM!");
            System.out.println("Por favor, selecione o tipo de plano para o USO DESTE CRM:");
            System.out.println("1. Gratuito (Acesso às funcionalidades básicas: 1-6)");
            System.out.println(
                    "2. Premium (Acesso total a todas as funcionalidades do CRM - requer ativação)");
            System.out.print("Escolha uma opção de plano para o CRM (1 ou 2): ");

            int planoEscolhido = 0;
            if (scanner.hasNextInt()) {
                planoEscolhido = scanner.nextInt();
            }
            scanner.nextLine(); // Consome a quebra de linha ou input inválido

            if (planoEscolhido == 2) {
                // Para o plano premium, vamos direcionar para a ativação simulada
                System.out.println("Plano Premium selecionado. Você será direcionado para a ativação/gerenciamento.");
                isPremiumUser = gerenciarPlanoAssinaturaCRM(scanner, isPremiumUser, crm);
            } else if (planoEscolhido == 1) {
                crm.cadastrarPlanoAssinaturaCRM("Gratuito", null); // Informa o CRM sobre o plano gratuito
                System.out.println(
                        "Plano Gratuito do CRM selecionado. Funcionalidades 7-11 requerem o plano Premium do CRM.");
            } else {
                crm.cadastrarPlanoAssinaturaCRM("Gratuito", null); // Default para gratuito
                System.out.println("Opção de plano inválida. Assumindo Plano Gratuito do CRM por padrão.");
                System.out.println("Funcionalidades 7-11 requerem o plano Premium do CRM.");
            }
            int opcao;
            limparTela();
            do {
                System.out.println("--- MENU PRINCIPAL ---");
                System.out.println("1. Adicionar Cliente");
                System.out.println("2. Adicionar Produto");
                System.out.println("3. Registrar Venda");
                System.out.println("4. Listar Clientes");
                System.out.println("5. Listar Produtos");
                System.out.println("6. Listar Vendas");
                System.out.println("--- Funcionalidades Premium do CRM ---");
                System.out
                        .println("7. Histórico de Compras do Cliente" + (isPremiumUser ? "" : " (Requer Premium)"));
                System.out
                        .println("8. Listar Clientes Mais Lucrativos" + (isPremiumUser ? "" : " (Requer Premium)"));
                System.out.println("9. Listar Produtos Mais Vendidos" + (isPremiumUser ? "" : " (Requer Premium)"));
                System.out
                        .println("10. Listar Períodos Mais Vendidos" + (isPremiumUser ? "" : " (Requer Premium)"));
                System.out.println("11. Prever Demanda Futura" + (isPremiumUser ? "" : " (Requer Premium)"));
                System.out.println(
                        "12. Gerar Relatório Estratégico Consolidado" + (isPremiumUser ? "" : " (Requer Premium)"));
                System.out.println("------------------------------------");
                System.out.println("13. Ativar/Gerenciar Plano Premium do CRM");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");

                if (scanner.hasNextInt()) {
                    opcao = scanner.nextInt();
                } else {
                    opcao = -1;
                }
                scanner.nextLine();

                switch (opcao) {
                    case 1: {
                        limparTela();
                        System.out.println("--- Adicionar Cliente ---");
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
                        limparTela();
                        System.out.println("--- Adicionar Produto ---");
                        System.out.print("Nome do Produto: ");
                        String nome = scanner.nextLine();
                        System.out.print("Preço: ");
                        double preco = 0;
                        if (scanner.hasNextDouble()) {
                            preco = scanner.nextDouble();
                        } else {
                            System.out.println("Entrada de preço inválida.");
                        }
                        scanner.nextLine();
                        System.out.print("Tipo (Descrição): ");
                        String tipo = scanner.nextLine();
                        if (preco > 0) {
                            crm.adicionarProduto(nome, preco, tipo);
                        }
                        break;
                    }
                    case 3: {
                        limparTela();
                        System.out.println("--- Registrar Venda ---");
                        System.out.print("ID do Cliente: ");
                        int idCliente = 0;
                        if (scanner.hasNextInt()) {
                            idCliente = scanner.nextInt();
                        } else {
                            System.out.println("ID de cliente inválido.");
                        }
                        scanner.nextLine();
                        System.out.print("ID do Produto: ");
                        int idProduto = 0;
                        if (scanner.hasNextInt()) {
                            idProduto = scanner.nextInt();
                        } else {
                            System.out.println("ID de produto inválido.");
                        }
                        scanner.nextLine();
                        System.out.print("Quantidade: ");
                        int qtdProd = 0;
                        if (scanner.hasNextInt()) {
                            qtdProd = scanner.nextInt();
                        } else {
                            System.out.println("Quantidade inválida.");
                        }
                        scanner.nextLine();

                        if (idCliente > 0 && idProduto > 0 && qtdProd > 0) {
                            Produto produto = crm.buscarProdutoPorId(idProduto);
                            if (produto != null) {
                                crm.registrarVenda(idCliente, idProduto, qtdProd, produto.getPreco() * qtdProd);
                            } else {
                                System.out.println("Produto não encontrado.");
                            }
                        } else {
                            System.out.println("Dados da venda inválidos. Venda não registrada.");
                        }
                        break;
                    }
                    case 4:
                        limparTela();
                        System.out.println("--- Listar Clientes ---");
                        crm.listarClientes();
                        break;
                    case 5:
                        limparTela();
                        System.out.println("--- Listar Produtos ---");
                        crm.listarProdutos();
                        break;
                    case 6:
                        limparTela();
                        System.out.println("--- Listar Vendas ---");
                        crm.listarVendas();
                        break;
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12: {
                        if (isPremiumUser) {
                            switch (opcao) {
                                case 7: {
                                    limparTela();
                                    System.out.println("--- Histórico de Compras do Cliente ---");
                                    System.out.print("Informe o ID do Cliente: ");
                                    int id = 0;
                                    if (scanner.hasNextInt()) {
                                        id = scanner.nextInt();
                                    } else {
                                        System.out.println("ID de cliente inválido.");
                                    }
                                    scanner.nextLine();
                                    if (id > 0) {
                                        Cliente cliente = crm.buscarClientePorId(id);
                                        if (cliente != null) {
                                            cliente.exibirHistoricoCompras();
                                        } else {
                                            System.out.println("Cliente não encontrado.");
                                        }
                                    }
                                    break;
                                }
                                case 8:
                                    limparTela();
                                    crm.listarClientesMaisLucrativos();
                                    break;
                                case 9:
                                    limparTela();
                                    crm.listarProdutosMaisVendidos();
                                    break;
                                case 10:
                                    limparTela();
                                    crm.listarPeriodosMaisVendidos();
                                    break;
                                case 11:
                                    limparTela();
                                    crm.preverDemandaFutura();
                                    break;
                                case 12:
                                    limparTela();
                                    crm.gerarRelatorioEstrategico();
                                    break;
                            }
                        } else {
                            limparTela();
                            System.out.println("Esta funcionalidade é exclusiva para o plano Premium do CRM.");
                            System.out.println("Por favor, ative o plano na opção 13 para acessá-la.");
                        }
                        break;
                    }
                    case 13: {
                        limparTela();
                        isPremiumUser = gerenciarPlanoAssinaturaCRM(scanner, isPremiumUser, crm);
                        break;
                    }
                    case 0:
                        limparTela();
                        System.out.println("Encerrando o sistema...");
                        System.exit(0);
                    default:
                        limparTela();
                        System.out.println("Opção inválida. Tente novamente.");
                }
            } while (opcao != 0);
        }
    }
}
