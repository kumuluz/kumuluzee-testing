package com.kumuluz.ee.testing.arquillian.utils;

import com.kumuluz.ee.testing.arquillian.assets.MainWrapper;
import com.kumuluz.ee.testing.arquillian.exceptions.ParsingException;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;

import java.io.*;
import java.util.concurrent.CountDownLatch;

//import org.jboss.arquillian.container.spi.client.container.DeploymentException;

public class OutputProcessor implements Runnable, Closeable {

    private InputStream stream;
    private CountDownLatch latch;

    private HTTPContext httpContext;
    private Throwable error;

    public OutputProcessor(InputStream stream, CountDownLatch latch) {
        this.stream = stream;
        this.latch = latch;
    }

    public HTTPContext getHttpContext() {
        return httpContext;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.stream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(MainWrapper.MSG_PREFIX)) {
                    processMessage(line.trim());
                } else {
                    System.out.println(line);
                }

                if (line.contains("DeploymentException")) { // TODO better detection
                    this.error = new javax.enterprise.inject.spi.DeploymentException("Error starting container");
                    this.latch.countDown();
                }
            }
        } catch (IOException | ParsingException e) {
            this.error = e;
            this.latch.countDown();
        }
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    private void processMessage(String message) throws ParsingException {
        if (message.equals(MainWrapper.MSG_SERVER_STARTED)) {
            this.latch.countDown();
        } else if (message.startsWith(MainWrapper.MSG_METADATA_PREFIX)) {
            String metadata = message.substring(MainWrapper.MSG_METADATA_PREFIX.length());

            String[] tokens = metadata.split("\t");

            int port;
            try {
                port = Integer.parseInt(tokens[0]);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new ParsingException("Could not parse port from string: " + metadata, e);
            }

            httpContext = new HTTPContext("localhost", port);

            for (int i = 1; i < tokens.length; i++) {
                if (!tokens[i].isEmpty()) {
                    String[] servletInfo = tokens[i].split(":");

                    if (servletInfo.length < 1) {
                        throw new ParsingException("Could not parse servlet information from token: " + tokens[i]);
                    }

                    httpContext.add(new Servlet(servletInfo[0], (servletInfo.length > 1) ? servletInfo[1] : ""));
                }
            }
        } else {
            throw new ParsingException("Could not parse message: " + message);
        }
    }
}
