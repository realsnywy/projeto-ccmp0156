# CRM Acadêmico

## 📚 Contexto

Este projeto foi desenvolvido em **Java 24** como parte da disciplina de **Planejamento e Gestão Estratégica**.

- **Curso:** Ciência da Computação
- **Período:** 2025.1
- **Instituição:** Universidade do Vale do São Francisco (UNIVASF)

## 💻 Sobre

O sistema é um **Customer Relationship Management (CRM)** criado como projeto acadêmico, com o objetivo de aprimorar o aprendizado em planejamento estratégico e desenvolvimento de software.

## ⚙️ Tecnologias Utilizadas

- **Linguagem:** Java 24
- **Build Tool:** Maven
- **Banco de Dados:** MariaDB com JDBC Driver

## 🌳 Estrutura do Projeto

```
projeto-ccmp0156/
├── .mvn/                     # Configurações do Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/             # Código fonte Java
│   │   │   └── com/univasf/sistemaVendas/
│   │   │       ├── ConexaoDB.java
│   │   │       └── ...       # Outras classes do projeto
│   │   └── resources/        # Arquivos de recursos (configuração, etc.)
│   └── test/
│       └── java/             # Código fonte de testes
├── target/                   # Arquivos gerados pelo build (classes compiladas, JARs)
├── .gitignore                # Arquivos e diretórios ignorados pelo Git
├── mvnw                      # Script do Maven Wrapper (Linux/macOS)
├── mvnw.cmd                  # Script do Maven Wrapper (Windows)
├── pom.xml                   # Arquivo de configuração do Maven
└── README.md                 # Este arquivo
```

## 🚀 Como Executar

Para executar o projeto utilizando o wrapper do Maven (`mvnw`), siga os passos abaixo:

1. **Configure o Banco de Dados MariaDB:**
    - Conecte-se ao MariaDB como root (será solicitada a senha):

        ```bash
        mysql -u root -p
        ```

    - Execute os seguintes comandos SQL para criar o banco de dados e o usuário necessário:

        ```sql
        CREATE DATABASE IF NOT EXISTS sistemaVendas;
        CREATE USER IF NOT EXISTS 'admin_sistema'@'localhost' IDENTIFIED BY 'S!st3m@2025';
        GRANT ALL PRIVILEGES ON sistemaVendas.* TO 'admin_sistema'@'localhost';
        FLUSH PRIVILEGES;
        EXIT;
        ```

    - **Importante:** Verifique se as credenciais (URL, usuário e senha) no arquivo `src/main/java/com/univasf/sistemaVendas/ConexaoDB.java` correspondem à sua configuração local. Ajuste se necessário.

2. Certifique-se de que você possui o Java 24 instalado e configurado no seu ambiente.
3. No terminal, navegue até o diretório raiz do projeto.
4. Execute os seguintes comandos:

    ```bash
    # Limpar e construir o projeto
    ./mvnw clean install

    # Executar o projeto (isso também executará o script de criação de tabelas)
    ./mvnw exec:java
    ```

> Este projeto está em desenvolvimento e é destinado exclusivamente para fins acadêmicos.
