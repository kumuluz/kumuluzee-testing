/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.testing.arquillian.assets;

import com.kumuluz.ee.EeApplication;
import com.kumuluz.ee.common.KumuluzServer;
import com.kumuluz.ee.common.ServletServer;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.servlet.ServletWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;

/**
 * Starts KumuluzEE Server and communicates runtime data through stdout.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
public class MainWrapper {

    public static final String MSG_PREFIX = "KumuluzEE Arquillian: ";
    public static final String MSG_SERVER_STARTED = MSG_PREFIX + "Server started";
    public static final String MSG_METADATA_PREFIX = MSG_PREFIX + "HTTP Context: ";
    public static final String MSG_EXCEPTION_PREFIX = MSG_PREFIX + "Exception thrown: ";

    public static void main(String[] args) {
        try {
            System.setProperty("kumuluzee.server.jetty.forward-startup-exception", "true");

            EeApplication app = new EeApplication();

            KumuluzServer server = app.getServer();
            if (server instanceof ServletServer) {
                // print metadata (gets intercepted by parent process)
                // print port
                Integer port = EeConfig.getInstance().getServer().getHttp().getPort();

                StringBuilder metadataSb = new StringBuilder();
                metadataSb.append(MSG_METADATA_PREFIX).append(port).append('\t');

                // print information about the servlets
                List<ServletWrapper> servlets = ((ServletServer) server).getRegisteredServlets();
                for (ServletWrapper s : servlets) {
                    metadataSb.append(s.getName()).append(':').append(s.getContextPath()).append('\t');
                }
                System.out.println(metadataSb.toString());
            }

            System.out.println(MSG_SERVER_STARTED);
        } catch (Exception e) {
            System.out.println(MSG_EXCEPTION_PREFIX + serializeException(e));
            throw e;
        }
    }

    private static String serializeException(Exception e) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(e);
            so.flush();

            return Base64.getEncoder().encodeToString(bo.toByteArray());
        } catch (IOException e1) {
            Exception newEx = new RuntimeException("IO error while serializing exception", e1);
            newEx.printStackTrace();
            return serializeException(newEx);
        }
    }
}
