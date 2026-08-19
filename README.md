![thumbnail-Mensageria com RabbitMQ](https://user-images.githubusercontent.com/66698429/193290606-c7424fe9-d793-436f-8bd7-164c84980359.png)

# ??? AluraFood - Microsserviços com Mensageria RabbitMQ

Projeto desenvolvido como parte da formação **Java e Microsserviços com Spring e RabbitMQ** da Alura. O projeto implementa uma arquitetura de microsserviços orientada a eventos para comunicação assíncrona entre serviços utilizando o **RabbitMQ**.

---

## ??? Arquitetura do Projeto

A solução é composta pelos seguintes componentes:

| Serviço / Componente | Descrição | Porta Local / Container |
| :--- | :--- | :--- |
| **RabbitMQ** | Broker de mensageria com painel de gerenciamento | 5672 (AMQP) / 15672 (Web UI) |
| **MySQL** | Banco de dados relacional para persistência | 3306 |
| **Eureka Server (server)** | Service Discovery / Registro de serviços | 8081 |
| **API Gateway (gateway)** | Ponto único de entrada e roteamento dinâmico | 8082 |
| **Pagamentos (pagamentos-ms)** | Microsserviço de gestão e criação de pagamentos (Produtor AMQP) | 8083 |
| **Pedidos (pedidos-ms)** | Microsserviço de gestão de pedidos (Consumidor AMQP) | 8084 |

---

## ?? Fluxo de Mensageria (RabbitMQ)

- **Fila**: pagamento.concluido (criada automaticamente pelo RabbitAdmin como não-durável).
- **Publicação**: Ao cadastrar um pagamento (POST /pagamentos), o pagamentos-ms envia uma mensagem informando o ID do pagamento gerado para a fila pagamento.concluido.
- **Consumo**: O pedidos-ms monitora a fila pagamento.concluido através do @RabbitListener para processar a confirmação de pagamento de forma assíncrona e desacoplada.

---

## ?? Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/) instalados.
- *(Opcional)* Java 17+ e Maven caso queira executar os serviços fora dos containers Docker.

---

## ?? Como Executar

### 1. Inicializando todos os serviços com Docker Compose

Na raiz do projeto, execute:

`ash
docker compose up -d --build
`

> **Nota:** O Docker Compose aguarda o MySQL e o RabbitMQ ficarem saudáveis (healthcheck) antes de iniciar os microsserviços.

### 2. Acessando os Painéis

- **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672)
  - **Usuário**: guest
  - **Senha**: guest
- **Eureka Service Registry**: [http://localhost:8081](http://localhost:8081)

### 3. Recompilando um serviço após alterações no código

Sempre que alterar arquivos .java de um serviço em container, reconstrua a imagem com:

`ash
docker compose up -d --build <nome-do-servico>
# Exemplo:
docker compose up -d --build pagamentos-ms
`

### 4. Parando os containers

`ash
docker compose down
`

---

## ?? Como Testar a Mensageria

### 1. Criando um Pagamento (Produtor)

Envie uma requisição POST para o endpoint através do API Gateway ou diretamente no serviço de pagamentos:

- **Endpoint**: http://localhost:8082/pagamentos-ms/pagamentos (ou http://localhost:8083/pagamentos)
- **Método**: POST
- **Headers**: Content-Type: application/json
- **Body**:
`json
{
  "valor": 120.50,
  "nome": "Cliente Teste",
  "numero": "1234567890123456",
  "expiracao": "12/28",
  "codigo": "123",
  "status": "CRIADO",
  "formaDePagamentoId": 1,
  "pedidoId": 1
}
`

### 2. Inspecionando a Fila no RabbitMQ

1. Acesse [http://localhost:15672](http://localhost:15672) e faça login (guest/guest).
2. Acesse a aba **Queues** e clique na fila pagamento.concluido.
3. Expanda a seção **Get messages**.
4. Defina **Requeue** como Yes (para manter a mensagem na fila após a visualização).
5. Clique em **Get Message(s)** para visualizar o payload gerado:
   `	ext
   Criei um pagamento com o id <ID_GERADO>
   `

---

## ??? Endpoints Principais

### Pagamentos (/pagamentos-ms/pagamentos)
- GET /pagamentos-ms/pagamentos - Lista pagamentos paginados
- GET /pagamentos-ms/pagamentos/{id} - Detalhes do pagamento
- POST /pagamentos-ms/pagamentos - Cadastra pagamento e publica no RabbitMQ
- PUT /pagamentos-ms/pagamentos/{id} - Atualiza dados do pagamento
- DELETE /pagamentos-ms/pagamentos/{id} - Remove pagamento
- PATCH /pagamentos-ms/pagamentos/{id}/confirmar - Confirma pagamento via CircuitBreaker

### Pedidos (/pedidos-ms/pedidos)
- GET /pedidos-ms/pedidos - Lista todos os pedidos
- GET /pedidos-ms/pedidos/{id} - Detalhes do pedido
- POST /pedidos-ms/pedidos - Cria novo pedido
- PUT /pedidos-ms/pedidos/{id}/status - Atualiza status do pedido

---

## ?? Links Úteis

- [Documentação Oficial do RabbitMQ](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP Reference](https://docs.spring.io/spring-amqp/docs/current/reference/html/)
- [Formação Java e Microsserviços com Spring e RabbitMQ - Alura](https://cursos.alura.com.br/formacao-java-microsservicos)
