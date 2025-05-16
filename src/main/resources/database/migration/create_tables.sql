-- Tabela Cliente
CREATE TABLE IF NOT EXISTS Cliente (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR (255) NOT NULL,
  email VARCHAR (255) UNIQUE NOT NULL,
  telefone VARCHAR (20),
  data_cadastro DATE NOT NULL,
  compras INT DEFAULT 0,
  -- Adicionado para rastrear o número de compras
  total_gasto DECIMAL(10, 2) DEFAULT 0.00 -- Adicionado para rastrear o total gasto
);
-- Tabela Produtos
CREATE TABLE IF NOT EXISTS Produtos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR (255) NOT NULL,
  descricao TEXT,
  preco DECIMAL (10, 2) NOT NULL,
  estoque INT NOT NULL,
  -- Embora não usado ativamente na lógica CRM atual, mantido por ora
  data_cadastro DATE NOT NULL
);
-- Tabela Vendas
CREATE TABLE IF NOT EXISTS Vendas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id INT NOT NULL,
  data_venda DATETIME NOT NULL,
  valor_total DECIMAL (10, 2) NOT NULL,
  FOREIGN KEY (cliente_id) REFERENCES Cliente (id)
);
-- Tabela ItensVenda
CREATE TABLE IF NOT EXISTS ItensVenda (
  id INT AUTO_INCREMENT PRIMARY KEY,
  venda_id INT NOT NULL,
  produto_id INT NOT NULL,
  quantidade INT NOT NULL,
  preco_unitario DECIMAL (10, 2) NOT NULL,
  FOREIGN KEY (venda_id) REFERENCES Vendas (id) ON DELETE CASCADE,
  -- Adicionado ON DELETE CASCADE
  FOREIGN KEY (produto_id) REFERENCES Produtos (id)
);
-- Tabela PlanosDeAssinatura
CREATE TABLE IF NOT EXISTS PlanosDeAssinatura (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  preco DECIMAL(10, 2) NOT NULL,
  duracao VARCHAR(50) NOT NULL
);
