CREATE TABLE ItensVenda (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venda_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL (10, 2) NOT NULL,
    FOREIGN KEY (venda_id) REFERENCES Vendas (id),
    FOREIGN KEY (produto_id) REFERENCES Produtos (id)
);
