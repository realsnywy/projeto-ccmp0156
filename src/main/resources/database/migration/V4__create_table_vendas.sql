CREATE TABLE Vendas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    data_venda DATETIME NOT NULL,
    valor_total DECIMAL (10, 2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES Cliente (id)
);
