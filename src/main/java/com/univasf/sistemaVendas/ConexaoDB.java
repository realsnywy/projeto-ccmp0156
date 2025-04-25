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
    private static final String USER = "admin_sistema";
    private static final String PASSWORD = "S!st3m@2025";
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

            // Lê e executa o script SQL
            StringBuilder sql = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                sql.append(linha).append("\n");
            }

            statement.execute(sql.toString());
            System.out.println("Script do banco de dados executado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao executar o script do banco de dados: " + e.getMessage());
        }
    }
}
