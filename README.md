# 📄 README.md

## Sistema Distribuído de Controle Colaborativo com Exclusão Mútua e Recuperação de Falhas

Este projeto é uma **simulação de acesso concorrente a um recurso crítico compartilhado** em um ambiente distribuído.  
Foi desenvolvido como atividade da disciplina **Sistemas Distribuídos (IFBA – Campus Santo Antônio de Jesus)**, atendendo aos requisitos do trabalho acadêmico.

### 🎯 Objetivo
- Simular múltiplos processos clientes acessando/modificando um **recurso crítico compartilhado**.  
- Garantir **exclusão mútua distribuída**.  
- Implementar **replicação de dados com consistência eventual**.  
- Realizar **checkpoints** e **rollback** em caso de falhas.  
- Utilizar **relógios de Lamport** para ordenação de requisições.

---

## 📂 Estrutura do Projeto
```
SimulacaoAcessoConcorrente-main/
│── program/
│   └── src/
│       ├── cliente/
│       │   └── NoCliente.java
│       ├── servidor/
│       │   └── ServidorCoordenador.java
│       ├── comum/
│       │   ├── Mensagem.java
│       │   └── RecursoCompartilhado.java
│       └── main/
│           └── Main.java
│── out/ (arquivos compilados)
│── checkpoint_ClienteX.dat (checkpoints gerados)
│── servidor_state.dat (estado do servidor)
```

---

## ⚙️ Tecnologias Utilizadas
- **Java 21**  
- **Sockets TCP/IP** para comunicação cliente-servidor  
- **Relógios Lógicos de Lamport**  
- **Serialização de objetos** para checkpoints  

---

## ▶️ Instruções de Compilação e Execução

### 1. Pré-requisitos
- Instalar **JDK 21**  
- Ter o **javac** e **java** configurados no `PATH`  

### 2. Compilar o projeto
Na raiz do projeto, execute:

```bash
cd SimulacaoAcessoConcorrente-main/program/src
javac */*.java main/*.java -d ../bin
```

Isso criará os arquivos `.class` dentro de `program/bin`.

### 3. Executar o servidor coordenador
```bash
cd ../bin
java main.Main servidor
```

### 4. Executar os clientes
Em **terminais separados**, inicie os clientes:

```bash
java main.Main cliente 1
java main.Main cliente 2
java main.Main cliente 3
java main.Main cliente 4
```

### 5. Simulação
- Cada cliente requisita acesso ao **recurso compartilhado**.  
- O **servidor coordenador** gerencia a exclusão mútua.  
- As operações são replicadas para os nós, com **consistência eventual**.  
- Em caso de falha, o estado pode ser restaurado via **checkpoints**.  

---

## 🧪 Funcionalidades Atendidas
- [x] Comunicação distribuída via TCP  
- [x] Exclusão mútua centralizada  
- [x] Replicação de dados com consistência eventual  
- [x] Checkpoints periódicos e rollback em falhas  
- [x] Controle de concorrência com relógios de Lamport  
- [x] Modularização em pacotes  
