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

public class Atwater {
    private static final String serverID = "ATW";
    private static final String serverName = "Atwater";
    private static final int Atwater_Server_Port = 8877;
    private static final String serverEndPoint = "http://localhost:8080/atwater";
    public static void main(String[] args) throws Exception{
        try {
            System.out.println(serverName + " Server Started...");
            MethodImplementation service = new MethodImplementation(serverID, serverName);

            Endpoint endpoint = Endpoint.publish(serverEndPoint, service);

            System.out.println(serverName + " Server is Up & Running");

//            addTestData(server);
            Runnable task = () -> {
                listenForRequest(service, Atwater_Server_Port, serverName, serverID);
            };
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
        }
    }

    private static void addTestData(MethodImplementation remoteObject) {
        remoteObject.addNewMovie("ATWA090223", MovieModel.AVATAR, 20);
        remoteObject.addNewMovie("ATWA090223", MovieModel.TITANIC, 20);
        remoteObject.addNewMovie("ATWE090223", MovieModel.AVENGERS, 20);
        remoteObject.addNewMovie("ATWA150223", MovieModel.TITANIC, 20);
    }

    private static void listenForRequest(MethodImplementation obj, int serverUdpPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
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
                    String result = obj.removeMovieUDP(movieID, movieType, customerID);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listMovieAvailability")) {
                    String result = obj.listMovieAvailabilityUDP(movieType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookMovie")) {
                    String result = obj.bookMovieTickets(customerID, movieID, movieType, movieCapacity);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelMovie")) {
                    String result = obj.cancelMovieTickets(customerID, movieID, movieType, movieCapacity);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
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
