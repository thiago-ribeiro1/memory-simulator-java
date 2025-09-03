# Simulador de Gerenciamento de Memória

Desenvolvemos um sistema em Java para simular o gerenciamento de memória. A interface gráfica foi criada com Java Swing
e exibe uma grade 8x8 de unidades, cada uma representando 2 KB, totalizando 128 KB de memória física.

---

<img width="598" height="452" alt="Image" src="https://github.com/user-attachments/assets/176d560a-8177-43c7-889e-85d587b88476" />

---
## Funcionamento

O usuário pode escolher entre três algoritmos de alocação:

- First Fit → aloca no primeiro espaço livre com tamanho adequado.
- Next Fit → aloca a partir da última posição usada.
- Best Fit → aloca no espaço livre que tenha o tamanho mais adequado que comporte o processo.

A cada alocação, o usuário define um ID (identificador do processo) e o tamanho em KB a ser reservado. O simulador
mostra graficamente os blocos ocupados e livres, além de uma tabela com os processos ativos e a memória que cada um
utiliza. Também exibimos o total de memória em uso e livre.

Além da inserção manual, é possível executar automaticamente operações pré-definidas através do botão “Gerar Carga”, e
acompanhar passo a passo com o “Step” ou rodar toda a sequência de uma vez com “Run”. O botão Reset limpa a memória e
retorna ao estado inicial.

## Estrutura Técnica

- Algoritmos de alocação: implementados via interface AllocationAlgorithm, com cada estratégia em uma classe distinta (
  FirstFit, NextFit, BestFit).
- LinkedList: a memória é modelada como uma LinkedList<MemoryBlock>, onde cada nó representa um bloco (livre ou
  ocupado). Essa estrutura facilita divisão (quando alocamos parte de um bloco) e junção (adjacência) de blocos
  vizinhos livres após liberações.
- Visualização: cada unidade de 2 KB é desenhada como uma célula colorida (executando) ou riscada em cinza (livre).

## Execução

- Requisitos: Java JDK 17
- Para executar: abra o projeto na sua IDE e rode a classe Main.java

## Como Rodar no Windows PowerShell (Opcional)

- Abra o PowerShell na raiz do projeto
- Execute este comando abaixo para compilar

```bash
javac -d out -encoding UTF-8 .\src\main\java\br\edu\unifacisa\*.java
```

- Em seguida use este outro comando para executar a aplicação

```bash
java -cp out br.edu.unifacisa.Main
```

## Considerações

No simulador todos os três funcionam, mas em termos de performance o First Fit é o mais rápido, já que para no primeiro 
espaço que encontra sem precisar percorrer toda a lista. O Next Fit tem uma performance parecida, só que ele continua a 
busca de onde parou na última alocação, evitando sobrecarregar sempre o início da memória. Já o Best Fit é o que mais 
tenta aproveitar bem os espaços, mas como precisa analisar todos os blocos livres, acaba sendo o mais lento.

Pensando em desempenho, o First Fit é o que se mostra melhor nesse projeto.



