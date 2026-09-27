package network; 
 
import java.net.Inet4Address; 
import java.net.Inet6Address;
import java.net.InetAddress; 
 
public class HostInspector { 
    public static void main(String[] args) { 
        if (args.length != 1) { 
            System.out.println("Usage: java network.HostInspector <hostname>"); 
            return; 
        } 
 
        try { 
            InetAddress[] addresses = InetAddress.getAllByName(args[0]); 
            System.out.println("Host: " + args[0]); 
 
            for (InetAddress address : addresses) { 
                System.out.println("- IP: " + address.getHostAddress()); 
                System.out.println("  Canonical: " 
                        + address.getCanonicalHostName()); 
                System.out.println("  Loopback: " 
                        + address.isLoopbackAddress()); 
                System.out.println("  Site local: " 
                        + address.isSiteLocalAddress()); 
                        if (address instanceof Inet4Address) {
                  System.out.println("  IP Type: IPv4");
              } else if (address instanceof Inet6Address) {
                  System.out.println("  IP Type: IPv6");
              }
            } 
        } catch (UnknownHostException e) { 
            System.err.println("Không phân giải được host: " + args[0]); 
        } 
    } 
} 