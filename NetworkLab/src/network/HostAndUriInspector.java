package network;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class HostAndUriInspector {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java network.HostAndUriInspector <URI_or_Hostname>");
            return;
        }

        String targetHost = args[0];

        // 1. Bóc tách các thành phần URI
        try {
            URI uri = new URI(args[0]);
            if (uri.getHost() != null) {
                targetHost = uri.getHost();
                System.out.println("--- THONG TIN URI ---");
                System.out.println("Scheme   : " + uri.getScheme());
                System.out.println("Host     : " + uri.getHost());
                System.out.println("Port     : " + (uri.getPort() == -1 ? "Mac dinh" : uri.getPort()));
                System.out.println("Path     : " + uri.getPath());
                System.out.println("Query    : " + uri.getQuery());
                System.out.println("Fragment : " + uri.getFragment());
                System.out.println("--------------------");
            }
        } catch (URISyntaxException e) {
            // Không phải định dạng URI đầy đủ thì giữ nguyên targetHost
        }

        // 2. Phân giải địa chỉ IP từ host thu được
        try {
            InetAddress[] addresses = InetAddress.getAllByName(targetHost);
            System.out.println("Host: " + targetHost);

            for (InetAddress address : addresses) {
                System.out.println("- IP: " + address.getHostAddress());
                System.out.println("  Canonical : " + address.getCanonicalHostName());
                System.out.println("  Loopback  : " + address.isLoopbackAddress());
                System.out.println("  Site local: " + address.isSiteLocalAddress());

                if (address instanceof Inet4Address) {
                    System.out.println("  IP Type   : IPv4");
                } else if (address instanceof Inet6Address) {
                    System.out.println("  IP Type   : IPv6");
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Không phân giải được host: " + targetHost);
        }
    }
}