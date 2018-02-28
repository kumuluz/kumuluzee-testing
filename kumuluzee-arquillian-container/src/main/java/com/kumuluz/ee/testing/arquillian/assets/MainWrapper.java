package com.kumuluz.ee.testing.arquillian.assets;

import com.kumuluz.ee.EeApplication;
import com.kumuluz.ee.common.KumuluzServer;
import com.kumuluz.ee.common.ServletServer;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.servlet.ServletWrapper;

import java.util.List;

public class MainWrapper {

    public static final String MSG_PREFIX = "KumuluzEE Arquillian: ";
    public static final String MSG_SERVER_STARTED = MSG_PREFIX + "Server started";
    public static final String MSG_METADATA_PREFIX = MSG_PREFIX + "HTTP Context: ";

    public static void main(String[] args) {
        EeApplication app = new EeApplication();

        KumuluzServer server = app.getServer();
        if (server instanceof ServletServer) {
            // print metadata (gets intercepted by parent process)
            // print port
            Integer port = EeConfig.getInstance().getServer().getHttp().getPort();
            System.out.print(MSG_METADATA_PREFIX + port + "\t");

            // print information about the servlets
            List<ServletWrapper> servlets = ((ServletServer) server).getRegisteredServlets();
            for (ServletWrapper s : servlets) {
                System.out.print(s.getName() + ":" + s.getContextPath() + "\t");
            }
            System.out.println();
        }

        System.out.println(MSG_SERVER_STARTED);
    }
}
