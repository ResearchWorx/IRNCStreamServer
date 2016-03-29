package edu.uky.irnc.streamserver.sresource;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static Pattern p = Pattern.compile("(\\d+), bytes");
    private static Gson gson = new GsonBuilder().create();

    //AMQP
    private String inExchange;
    private String amqp_server;
    private String amqp_login;
    private String amqp_password;
    private String query_string;

    //ESPER
    private EPRuntime cepRT;
    private EPAdministrator cepAdm;
    private EPStatement cepStatement;

    private static Map<String, ESPERNetFlow> instances = new HashMap<String, ESPERNetFlow>();

    public ESPERNetFlow(String amqp_server, String amqp_login, String amqp_password, String inExchange, String query_string) {
        this.amqp_server = amqp_server;
        this.amqp_login = amqp_login;
        this.amqp_password = amqp_password;
        this.inExchange = inExchange;
        this.query_string = query_string;

        instances.put(inExchange, this);

        Stream.updateLatest(this.inExchange, new EventBean[0]);

        //START ESPER

        //The Configuration is meant only as an initialization-time object.
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("netFlow", netFlow.class.getName());
        EPServiceProvider cep = EPServiceProviderManager.getProvider(this.inExchange + "_provider", cepConfig);
        cepRT = cep.getEPRuntime();
        cepAdm = cep.getEPAdministrator();

        //END ESPER
    }

    public void run() {
        try {
            ConnectionFactory factory;
            Connection connection;
            QueueingConsumer consumer;

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

            System.out.println("Input Exchange: " + inExchange + " output console");

            while (Main.ESPERActive) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery(500);
                    if (delivery != null) {
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

    private void updateQuery(String limit) {
        System.out.println("Updating limit on [" + inExchange + "]");
        Matcher m = p.matcher(query_string);
        StringBuffer result = new StringBuffer();
        if (m.find()) {
            m.appendReplacement(result, limit + ", bytes");
        }
        m.appendTail(result);
        query_string = result.toString();
        try {
            if (cepStatement != null) {
                cepStatement.removeAllListeners();
                cepStatement.destroy();
            }
            cepStatement = cepAdm.createEPL(query_string);
            CEPListener c = new CEPListener(inExchange);
            cepStatement.addListener(c);
            System.out.println("Query [" + inExchange + "]: \"" + query_string + "\"");
        } catch (Exception ex) {
            System.out.println("Failed to add Query: \"" + query_string + "\"");
            ex.printStackTrace();
        }
    }

    public static void updateAllQuery(String limit) {
        for (ESPERNetFlow entry : instances.values()) {
            entry.updateQuery(limit);
        }
    }

    private class CEPListener implements UpdateListener {
        private String exchange;
        CEPListener(String exchange) {
            this.exchange = exchange;
        }

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            //System.out.println("Updating latest on [" + this.exchange + "]");
            if (newEvents != null) {
                Stream.updateLatest(this.exchange, newEvents);
            }
            if (oldEvents != null) {
                System.out.println("Old Event received: " + oldEvents[0].getUnderlying());
            }
        }
    }

    private void input(String inputStr) throws ParseException {
        try {
            netFlow flow = nFlowFromJson(inputStr);
            //System.out.println("Adding flow [" + inExchange + "] from " + flow.ip_src + " to " + flow.ip_dst);
            cepRT.sendEvent(flow);
        } catch (Exception ex) {
            System.out.println("ESPEREngine : Input netFlow Error : " + ex.toString());
            System.out.println("ESPEREngine : Input netFlow Error : InputStr " + inputStr);
        }
    }

    private netFlow nFlowFromJson(String json) {
        return gson.fromJson(json, netFlow.class);
    }
}
