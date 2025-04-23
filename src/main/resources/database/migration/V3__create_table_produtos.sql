CREATE TABLE Produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR (255) NOT NULL,
    descricao TEXT,
    preco DECIMAL (10, 2) NOT NULL,
    estoque INT NOT NULL,
    data_cadastro DATE NOT NULL
);