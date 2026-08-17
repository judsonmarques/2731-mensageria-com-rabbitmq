package br.com.alurafood.pagamentos.amqp;


import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;



@Configuration
public class PagamentoAMQPConfiguration {


   /*

    Define a fila "pagamento.concluido" como nao duravel,

    ou seja, ela sera perdida caso o broker do RabbitMQ seja reiniciado.
*/
    @Bean
    public Queue criaFila(){


        return QueueBuilder.nonDurable("pagamento.concluido").build();
    }

    /*

    Instancia o RabbitAdmin para gerenciar e registrar automaticamente

    os componentes de infraestrutura (filas, exchanges e bindings) no RabbitMQ.
            */
    @Bean
    public RabbitAdmin criaRabbitAdmin(ConnectionFactory conn) {
        return new RabbitAdmin(conn);
    }

    /*

    Inicializa a infraestrutura do RabbitMQ (filas, exchanges e bindings)

    assim que a aplicacao estiver totalmente carregada, evitando a criacao tardia (lazy).

            */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> inicializaAdmin(RabbitAdmin rabbitAdmin){
        return event -> rabbitAdmin.initialize();
    }
}
