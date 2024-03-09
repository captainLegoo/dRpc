package com.dcy.rpc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;

/**
 * @author Kyle
 * @date 2024/03/09
 * <p>
 * get host address
 */
public class GetHostAddress {
    /**
     * get local ip address
     */
    public static String getLocalIPAddress() {
        try {
            // Get intranet IP address
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get external ip address
     * @return
     * @throws IOException
     */
    public static String getExternalIPAddress() throws IOException {
        try {
            // Obtained by visiting a website that obtains the external IP address
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            return br.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
