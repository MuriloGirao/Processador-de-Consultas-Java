# Simulador de Consultas SQL em Java

Este projeto implementa um **simulador de consultas SQL** escrito em **Java**, cujo objetivo é demonstrar as etapas internas do processamento de uma consulta em um **Sistema Gerenciador de Banco de Dados (SGBD)**.  

O simulador não executa consultas em um banco de dados real, mas sim **modela as etapas teóricas** que ocorrem desde a entrada de uma consulta SQL até a geração de um plano de execução otimizado.

---

## ✨ Funcionalidades

O sistema foi projetado para simular as seguintes etapas:

### 1. Entrada e Validação da Consulta
- Recebe uma consulta SQL de entrada.
- Realiza análise sintática e semântica para verificar:
  - Estrutura correta da query.
  - Existência de tabelas e atributos mencionados.
- Retorna mensagens de erro caso a consulta seja inválida.

### 2. Conversão para Álgebra Relacional
- Transforma a consulta SQL validada em uma representação equivalente em **álgebra relacional**.
- Gera operações como:
  - Seleção (σ)
  - Projeção (π)
  - Junção (⨝)
  - União (∪)
  - Diferença (−)

### 3. Construção do Grafo de Operadores
- Constrói um **grafo de operadores** representando a consulta.
- Cada nó do grafo corresponde a uma operação da álgebra relacional.
- As arestas representam o fluxo de dados entre as operações.

### 4. Otimização da Consulta
- Aplica **regras heurísticas de otimização**, tais como:
  - Aplicar seleções o mais cedo possível.
  - Reordenar junções para minimizar custo.
  - Eliminar operações redundantes.
- Gera um grafo otimizado com custo estimado menor.

### 5. Plano de Execução
- Produz um **plano de execução** a partir do grafo otimizado.
- O plano descreve a ordem das operações a serem realizadas.
- Pode ser exibido em:
  - Forma textual (árvore de operadores).
  - Visualização gráfica do plano.

---

## 🛠️ Tecnologias Utilizadas
- **Java 17+**
- Estruturas de dados em Java para grafos

---

## 📖 Exemplo de Uso

### Entrada:
```sql
SELECT nome, idade
FROM Alunos
WHERE idade > 18;
````

---

## 📌 Objetivo Educacional

Este projeto tem caráter didático, servindo como uma ferramenta para compreensão dos principais conceitos de processamento e otimização de consultas em bancos de dados relacionais.
