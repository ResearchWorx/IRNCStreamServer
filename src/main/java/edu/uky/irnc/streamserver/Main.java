
package edu.uky.irnc.streamserver;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import edu.uky.irnc.streamserver.core.Config;
import edu.uky.irnc.streamserver.sresource.ESPERNetFlow;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;


public class Main {
    public static boolean ESPERActive = true;

    private static int getPort(int defaultPort) {
        //grab port from environment, otherwise fall back to default port 9998
        String httpPort = System.getProperty("jersey.test.port");
        if (null != httpPort) {
            try {
                return Integer.parseInt(httpPort);
            } catch (NumberFormatException e) {
                System.err.println("Port [" + httpPort + "] already bound");
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://0.0.0.0/").port(getPort(9998)).build();
    }

    private static final URI BASE_URI = getBaseURI();
    
    private static HttpServer startServer() throws IOException {
        ResourceConfig resourceConfig = new PackagesResourceConfig("edu.uky.irnc.streamserver.controllers");

        System.out.println("Starting grizzly2...");
        return GrizzlyServerFactory.createHttpServer(BASE_URI, resourceConfig);
    }


    private static String checkConfig(String[] args) {
        String errorMgs = "StreamReporter\n" +
                "Usage: java -jar StreamReporter.jar" +
                " -f <configuration_file>\n";

        if (args.length != 2) {
            System.err.println(errorMgs);
            System.err.println("ERROR: Invalid number of arguements.");
            System.exit(1);
        } else if (!args[0].equals("-f")) {
            System.err.println(errorMgs);
            System.err.println("ERROR: Must specify configuration file.");
            System.exit(1);
        } else {
            File f = new File(args[1]);
            if (!f.exists()) {
                System.err.println("The specified configuration file: " + args[1] + " is invalid");
                System.exit(1);
            }
        }
        return args[1];
    }
    
    public static void main(String[] args) throws IOException {
        Config conf;

        //create config
        conf = new Config(checkConfig(args));

        String amqp_server = conf.getConfig("amqp", "server");
        String amqp_login = conf.getConfig("amqp", "login");
        String amqp_password = conf.getConfig("amqp", "password");
        int exchange_count = Integer.parseInt(conf.getConfig("amqp", "exchanges"));

        String querystring = conf.getConfig("cep", "querystring");

        for (int i = 0; i < exchange_count; i++) {
            String ampq_inexchange = conf.getConfig("exchange" + i, "inexchange");
            new Thread(new ESPERNetFlow(amqp_server, amqp_login, amqp_password, ampq_inexchange, querystring)).start();
        }

        ESPERNetFlow.updateAllQuery("10");

        // Grizzly 2 initialization
        startServer();
    }    
}
