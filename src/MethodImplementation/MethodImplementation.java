package MethodImplementation;

import Interface.ClientInterface;
import Models.ClientModel;
import Models.MovieModel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MethodImplementation extends UnicastRemoteObject implements ClientInterface {
    private String serverID;
    private String serverName;
    private static final int Atwater_Server_Port = 8877;
    private static final int Outremont_Server_Port = 7788;
    private static final int Verdun_Server_Port = 6677;

    // HashMap<MovieType, HashMap <MovieID, Movie>>
    private Map<String, Map<String, MovieModel>> allMovies;

    // HashMap<CustomerID, HashMap <MovieType, List<MovieID>>>
    private Map<String, Map<String, List<String>>> clientMovies;

    // HashMap<ClientID, Client>
    private Map<String, ClientModel> serverClients;

    public MethodImplementation(String serverID, String serverName) throws RemoteException{
        super();
        this.serverID = serverID;
        this.serverName = serverName;
        allMovies = new ConcurrentHashMap<>();
        allMovies.put(MovieModel.AVATAR, new ConcurrentHashMap<>());
        allMovies.put(MovieModel.AVENGERS, new ConcurrentHashMap<>());
        allMovies.put(MovieModel.TITANIC, new ConcurrentHashMap<>());
        clientMovies = new ConcurrentHashMap<>();
        serverClients = new ConcurrentHashMap<>();
    }

    private static int getServerPort(String server) {
        if (server.equalsIgnoreCase("ATW")) {
            return Atwater_Server_Port;
        } else if (server.equalsIgnoreCase("OUT")) {
            return Outremont_Server_Port;
        } else if (server.equalsIgnoreCase("VER")) {
            return Verdun_Server_Port;
        }
        return 1;
    }

    // --------------------Interface Implementation------------------------------

    public String addMovieSlots(String movieID, String movieName, int bookingCapacity){
        return null;
    }
    public String removeMovieSlots(String movieID, String movieName){
        return null;
    }
    public String listMovieShowAvailability(String movieName){
        return null;
    }
    public String bookMovieTickets(String customerID, String movieID, String movieName, int noOfTickets) {
        return null;
    }
    public String getBookingSchedule(String customerID){
        return null;
    }
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int noOfTickets){
        return null;
    }


    //--------------------------- UDP RELATED FUNCTIONS ------------------------------------------------

    private String sendUDPMessage(int serverPort, String method, String customerID, String movieType, String movieId) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieType + ";" + movieId;
/*        try {
            Logger.serverLog(serverID, customerID, " UDP request sent " + method + " ", " eventID: " + movieId + " eventType: " + movieType + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
/*        try {
            Logger.serverLog(serverID, customerID, " UDP reply received" + method + " ", " eventID: " + eventId + " eventType: " + eventType + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return result;

    }

    private String getNextSameEvent(Set<String> keySet, String movieType, String oldMovieID) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(oldMovieID);
        Collections.sort(sortedIDs, new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                Integer timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(oldMovieID) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!allMovies.get(movieType).get(sortedIDs.get(i)).isFull()) {
                return sortedIDs.get(i);
            }
        }
        return "Failed";
    }

    private boolean exceedWeeklyLimit(String customerID, String movieDate) {
        int limit = 0;
        for (int i = 0; i < 3; i++) {
            List<String> registeredIDs = new ArrayList<>();
            switch (i) {
                case 0:
                    if (clientMovies.get(customerID).containsKey(MovieModel.AVATAR)) {
                        registeredIDs = clientMovies.get(customerID).get(MovieModel.AVATAR);
                    }
                    break;
                case 1:
                    if (clientMovies.get(customerID).containsKey(MovieModel.AVENGERS)) {
                        registeredIDs = clientMovies.get(customerID).get(MovieModel.AVENGERS);
                    }
                    break;
                case 2:
                    if (clientMovies.get(customerID).containsKey(MovieModel.TITANIC)) {
                        registeredIDs = clientMovies.get(customerID).get(MovieModel.TITANIC);
                    }
                    break;
            }
            for (String eventID :
                    registeredIDs) {
                if (eventID.substring(6, 8).equals(movieDate.substring(2, 4)) && eventID.substring(8, 10).equals(movieDate.substring(4, 6))) {
                    int week1 = Integer.parseInt(eventID.substring(4, 6)) / 7;
                    int week2 = Integer.parseInt(movieDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(day2 - day1);
                    if (week1 == week2) {
                        limit++;
                    }
                }
                if (limit == 3)
                    return true;
            }
        }
        return false;
    }

    private void addCustomersToNextSameEvent(String oldEventID, String eventType, List<String> registeredClients) throws RemoteException {
        for (String customerID :
                registeredClients) {
            if (customerID.substring(0, 3).equals(serverID)) {
                clientMovies.get(customerID).get(eventType).remove(oldEventID);
                String nextSameEventResult = getNextSameEvent(allMovies.get(eventType).keySet(), eventType, oldEventID);
                if (nextSameEventResult.equals("Failed")) {
                    return;
                } else {
                    //todo
                    // bookMovieTickets(customerID, nextSameEventResult, eventType);
                }
            } else {
                sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeEvent", customerID, eventType, oldEventID);
            }
        }
    }

    //--------------------------- UDP Specific functions (ONLY FOR UDP CALLS)---------------------------

    public String removeMovieUDP(String oldEventID, String eventType, String customerID) throws RemoteException {
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
            return "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
        } else {
            if (clientMovies.get(customerID).get(eventType).remove(oldEventID)) {
                return "Success: Event " + oldEventID + " Was Removed from " + customerID + " Schedule";
            } else {
                return "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
            }
        }
    }

    public String listMovieAvailabilityUDP(String eventType) throws RemoteException {
        Map<String, MovieModel> events = allMovies.get(eventType);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + eventType + ":\n");
        if (events.size() == 0) {
            builder.append("No Events of Type " + eventType);
        } else {
            for (MovieModel event :
                    events.values()) {
                builder.append(event.toString() + " || ");
            }
        }
        builder.append("\n=====================================\n");
        return builder.toString();
    }

    //--------------------------- EXTRA FUNCTIONS (TEST DATA)-----------------------------------

    public void addNewMovie(String movieID, String movieType, int capacity) {
        MovieModel sampleConf = new MovieModel(movieType, movieID, capacity);
        allMovies.get(movieType).put(movieID, sampleConf);

    }

    public void addNewCustomerToClients(String customerID) {
        ClientModel newCustomer = new ClientModel(customerID);
        serverClients.put(newCustomer.getClientID(), newCustomer);
        clientMovies.put(newCustomer.getClientID(), new ConcurrentHashMap<>());
    }

    //--------------------------HASH MAP GETTERS--------------------------------------------

    public Map<String, Map<String, MovieModel>> getAllMovies() {
        return allMovies;
    }

    public Map<String, Map<String, List<String>>> getClientMovies() {
        return clientMovies;
    }

    public Map<String, ClientModel> getServerClients() { return serverClients; }


}
