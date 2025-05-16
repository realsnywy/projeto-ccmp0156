package com.univasf.sistemaVendas;

public class PlanoDeAssinatura {
  private int id;
  private String nome;
  private double preco;
  private String duracao;

  public PlanoDeAssinatura(String nome, double preco, String duracao) {
    this.nome = nome;
    this.preco = preco;
    this.duracao = duracao;
  }

  public int getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public double getPreco() {
    return preco;
  }

  public String getDuracao() {
    return duracao;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public void setPreco(double preco) {
    this.preco = preco;
  }

  public void setDuracao(String duracao) {
    this.duracao = duracao;
  }

  @Override
  public String toString() {
    return "PlanoDeAssinatura{" +
        "id=" + id +
        ", nome='" + nome + '\'' +
        ", preco=" + preco +
        ", duracao='" + duracao + '\'' +
        '}';
  }
}
