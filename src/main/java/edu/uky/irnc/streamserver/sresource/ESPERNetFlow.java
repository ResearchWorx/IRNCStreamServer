package edu.uky.irnc.streamserver.sresource;

import java.text.ParseException;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import edu.uky.irnc.streamserver.Main;
import edu.uky.irnc.streamserver.controllers.Stream;

public class ESPERNetFlow implements Runnable {
    //AMQP
    private String inExchange;
    private String amqp_server;
    private String amqp_login;
    private String amqp_password;
    private String query_string;

    //ESPER
    private static EPRuntime cepRT;
    private static Gson gson;

    public ESPERNetFlow(String amqp_server, String amqp_login, String amqp_password, String inExchange, String query_string) {
        this.amqp_server = amqp_server;
        this.amqp_login = amqp_login;
        this.amqp_password = amqp_password;
        this.inExchange = inExchange;
        this.query_string = query_string;

        gson = new GsonBuilder().create();
    }

    public void run() {
        try {
            ConnectionFactory factory;
            Connection connection;
            QueueingConsumer consumer;
            EPAdministrator cepAdm;

            // START AMQP
            factory = new ConnectionFactory();
            factory.setHost(amqp_server);
            factory.setUsername(amqp_login);
            factory.setPassword(amqp_password);
            factory.setConnectionTimeout(10000);
            connection = factory.newConnection();

            //RX Channel
            Channel rx_channel = connection.createChannel();
            rx_channel.exchangeDeclare(inExchange, "fanout");
            String queueName = rx_channel.queueDeclare().getQueue();
            rx_channel.queueBind(queueName, inExchange, "");

            consumer = new QueueingConsumer(rx_channel);
            rx_channel.basicConsume(queueName, true, consumer);
            //END RX
            // END AMQP

            //START ESPER

            //The Configuration is meant only as an initialization-time object.
            Configuration cepConfig = new Configuration();
            cepConfig.addEventType("netFlow", netFlow.class.getName());
            EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
            cepRT = cep.getEPRuntime();
            cepAdm = cep.getEPAdministrator();

            //END ESPER

            System.out.println("ESPEREngine: Active");
            System.out.println("Input Exchange: " + inExchange + " output console");

            try {
                EPStatement cepStatement = cepAdm.createEPL(query_string);
                CEPListener c = new CEPListener();
                cepStatement.addListener(c);
                System.out.println("Added Query: \"" + query_string + "\"");
            } catch (Exception ex) {
                System.out.println("Failed to add Query: \"" + query_string + "\"");
                ex.printStackTrace();
            }
            while (Main.ESPERActive) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery(500);
                    if (!(delivery == null)) {
                        String message = new String(delivery.getBody());
                        //pass messages to processor
                        input(message);
                    }
                } catch (Exception ex) {
                    String errorString = "QueryNode: Error: " + ex.toString();
                    System.out.println(errorString);

                }
            }
        } catch (Exception ex) {
            System.out.println("QueryNode Error: " + ex.toString());
        }
    }

    private static class CEPListener implements UpdateListener {
        CEPListener() {

        }

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            if (newEvents != null) {
                Stream.updateLatest(newEvents);
            }
            if (oldEvents != null) {
                System.out.println("Old Event received: " + oldEvents[0].getUnderlying());
            }
        }
    }

    private static void input(String inputStr) throws ParseException {
        try {
            netFlow flow = nFlowFromJson(inputStr);
            cepRT.sendEvent(flow);
        } catch (Exception ex) {
            System.out.println("ESPEREngine : Input netFlow Error : " + ex.toString());
            System.out.println("ESPEREngine : Input netFlow Error : InputStr " + inputStr);
        }

    }

    private static netFlow nFlowFromJson(String json) {
        return gson.fromJson(json, netFlow.class);
    }
}
