package Server;

import MethodImplementation.MethodImplementation;
import Models.MovieModel;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.Naming;

public class Verdun {
    private static final String serverID = "VER";
    private static final String serverName = "Verdun";
    private static final int Verdun_Server_Port = 6677;
    private static final String serverEndPoint = "http://localhost:8082/verdun";
    public static void main(String[] args) throws Exception{
        try {
            System.out.println(serverName + " Server Started...");
            MethodImplementation service = new MethodImplementation(serverID, serverName);

            Endpoint endpoint = Endpoint.publish(serverEndPoint, service);

            System.out.println(serverName + " Server is Up & Running");

//            addTestData(server);
            Runnable task = () -> {
                listenForRequest(service, Verdun_Server_Port, serverName, serverID);
            };
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
        }
    }

    private static void addTestData(MethodImplementation remoteObject) {
        remoteObject.addNewMovie("VERA120223", MovieModel.AVATAR, 20);
        remoteObject.addNewMovie("VERA120223", MovieModel.TITANIC, 20);
        remoteObject.addNewMovie("VERM120223", MovieModel.AVATAR, 20);
        remoteObject.addNewMovie("VERE120223", MovieModel.TITANIC, 20);
    }

    private static void listenForRequest(MethodImplementation obj, int serverUdpPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            // Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String movieType = parts[2];
                String movieID = parts[3];
                int movieCapacity = Integer.parseInt(parts[4]);
                if (method.equalsIgnoreCase("removeMovie")) {
                    // Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.removeMovieUDP(movieID, movieType, customerID);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listMovieAvailability")) {
                    // Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventType: " + eventType + " ", " ...");
                    String result = obj.listMovieAvailabilityUDP(movieType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookMovie")) {
                    // Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.bookMovieTickets(customerID, movieID, movieType, movieCapacity);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelMovie")) {
                    // Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.cancelMovieTickets(customerID, movieID, movieType, movieCapacity);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                // Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", sendingResult);
            }
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
