package com.univasf.sistemaVendas;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados.
 */
public class ConexaoDB {
    private static final String URL = "jdbc:mariadb://localhost:3306/sistemaVendas";
    private static final String USER = "root";
    private static final String PASSWORD = "21040810";
    private static final String SCRIPT_PATH = "src/main/resources/database/migration/create_tables.sql";

    /**
     * Método estático para obter uma conexão com o banco de dados.
     *
     * @return Objeto Connection para interagir com o banco de dados.
     * @throws SQLException Caso ocorra algum erro ao tentar estabelecer a conexão.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Executa o script SQL de criação de tabelas, se ainda não foi executado.
     */
    public static void runDatabaseScript() {
        try (Connection connection = getConnection();
                Statement statement = connection.createStatement();
                BufferedReader reader = new BufferedReader(new FileReader(SCRIPT_PATH))) {

            // Lê o script SQL e divide em comandos individuais
            StringBuilder sql = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                sql.append(linha).append("\n");
            }

            // Divide o script em comandos individuais usando o ponto e vírgula
            String[] commands = sql.toString().split(";");
            for (String command : commands) {
                if (!command.trim().isEmpty()) { // Ignora comandos vazios
                    statement.execute(command.trim());
                }
            }

            System.out.println("Script do banco de dados executado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao executar o script do banco de dados: " + e.getMessage());
        }
    }
}
