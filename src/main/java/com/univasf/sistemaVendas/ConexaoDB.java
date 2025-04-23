package com.univasf.sistemaVendas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados.
 */
public class ConexaoDB {
    // URL de conexão com o banco de dados MariaDB, incluindo o endereço, porta e
    // nome do banco.
    private static final String URL = "jdbc:mariadb://localhost:3306/sistemaVendas";

    // Usuário para autenticação no banco de dados.
    private static final String USER = "admin_sistema";

    // Senha para autenticação no banco de dados.
    private static final String PASSWORD = "S!st3m@2025";

    /**
     * Método estático para obter uma conexão com o banco de dados.
     * 
     * @return Objeto Connection para interagir com o banco de dados.
     * @throws SQLException Caso ocorra algum erro ao tentar estabelecer a conexão.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}