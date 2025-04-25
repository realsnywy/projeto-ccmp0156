CREATE TABLE Cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR (255) NOT NULL,
    email VARCHAR (255) UNIQUE NOT NULL,
    telefone VARCHAR (20),
    data_cadastro DATE NOT NULL
);
