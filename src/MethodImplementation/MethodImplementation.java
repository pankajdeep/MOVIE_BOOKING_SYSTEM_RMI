package MethodImplementation;

import Interface.ClientInterface;
import Models.ClientModel;
import Models.MovieModel;
import movieTicketBookingInterfaceApp.movieTicketBookingInterfacePOA;
import org.omg.CORBA.ORB;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MethodImplementation extends movieTicketBookingInterfacePOA {
    private String serverID;
    private String serverName;
    public static HashMap<String,String> file = new HashMap<>();

    public String log;
    public String Status;

    static {
        file.put("ATW","ATWserver.txt");
        file.put("VER", "VERserver.txt");
        file.put("OUT", "OUTserver.txt");
    }
    private static final int Atwater_Server_Port = 8877;
    private static final int Outremont_Server_Port = 7788;
    private static final int Verdun_Server_Port = 6677;

    // HashMap<MovieType, HashMap <MovieID, Movie>>
    private Map<String, Map<String, MovieModel>> allMovies;

    // HashMap<CustomerID, HashMap <MovieType, HashMap <MovieID, TicketQty>>>
    private Map<String, Map<String, Map<String, Integer>>> clientMovies;

    // HashMap<ClientID, Client>
    private Map<String, ClientModel> serverClients;

    public MethodImplementation(String serverID, String serverName) {
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

    private ORB orb;
    public void setORB(ORB orb_val) {
        orb = orb_val;
    }
    public void shutdown() {
        orb.shutdown(false);
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

    public String addMovieSlots(String movieID, String movieType, int bookingCapacity) {
        log = "Slots added.";
        Status = "Passed";

        String response;
        if (allMovies.get(movieType).containsKey(movieID)) {
            if (allMovies.get(movieType).get(movieID).getMovieCapacity() <= bookingCapacity) {
                allMovies.get(movieType).get(movieID).setMovieCapacity(bookingCapacity);
                writeToLog("addMovieSlots",movieID+" "+movieType+" "+bookingCapacity,Status,bookingCapacity + " slots for movie " + movieType + " by movie ID " + movieID + " have been added");
                response = "Success: Movie " + movieID + " Capacity increased to " + bookingCapacity;
                /* try {
                    Logger.serverLog(serverID, "null", " RMI addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            } else {
                log = "Slots not added.";
                Status = "Failed";
                writeToLog("addMovieSlots",movieID+" "+movieType+" "+bookingCapacity,Status,bookingCapacity + " slots for movie " + movieType + " by movie ID " + movieID + " can't be added because movie capacity can't be decreased");
                response = "Failed: Movie Already Exists, Cannot Decrease Booking Capacity";
                /*try {
                    Logger.serverLog(serverID, "null", " RMI addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            return response;
        }
        if (MovieModel.detectMovieServer(movieID).equals(serverName)) {
            if(isWithinAWeek(movieID)) {
                MovieModel event = new MovieModel(movieType, movieID, bookingCapacity);
                Map<String, MovieModel> movieHashMap = allMovies.get(movieType);
                movieHashMap.put(movieID, event);
                allMovies.put(movieType, movieHashMap);
                log = "Slots added.";
                Status = "Passed";
                writeToLog("addMovieSlots",movieID+" "+movieType+" "+bookingCapacity,Status,bookingCapacity + " slots for movie " + movieType + " by movie ID " + movieID + " have been added");
                response = "Success: Event " + movieID + " added successfully";
            } else {
                log = "Slots not added.";
                Status = "Failed";
                writeToLog("addMovieSlots",movieID+" "+movieType+" "+bookingCapacity,Status,bookingCapacity + " slots for movie " + movieType + " by movie ID " + movieID + " can't be added because because you can only add a schedule within a week from today");
                response = "Failed: Cannot Add Movies to Server because you can only add a schedule within a week from today";
            }
        } else {
            log = "Slots not added.";
            Status = "Failed";
            writeToLog("addMovieSlots",movieID+" "+movieType+" "+bookingCapacity,Status,bookingCapacity + " slots for movie " + movieType + " by movie ID " + movieID + " can't be added because of server mismatch");
            response = "Failed: Cannot Add Movies to servers other than " + serverName;
        }
        return response;
    }
    public String removeMovieSlots(String movieID, String movieType) {
        log = "Slots Removed.";
        Status = "Passed";
        String response;
        if (MovieModel.detectMovieServer(movieID).equals(serverName)) {
            if (allMovies.get(movieType).containsKey(movieID)) {
                List<String> registeredClients = allMovies.get(movieType).get(movieID).getRegisteredClientIDs();
                allMovies.get(movieType).remove(movieID);
                addCustomersToNextSameEvent(movieID, movieType, registeredClients);
                writeToLog("removeMovieSlots",movieID+" "+movieType+" ",Status, "Movie Slots Removed for " + movieID);
                response = "Success: Movie Removed Successfully";

            } else {
                log= "Slots not Removed";
                Status = "Failed";
                writeToLog("removeMovieSlots",movieID+" "+movieType+" ",Status, "Movie Slots cant be Removed for " + movieID);
                response = "Failed: Movie " + movieID + " Does Not Exist";

            }
        } else {
            log= "Slots not Removed";
            Status = "Failed";
            writeToLog("removeMovieSlots",movieID+" "+movieType+" ",Status, "Movie Slots cant be Removed for " + movieID);
            response = "Failed: Cannot Remove Movie from servers other than " + serverName;

        }
        return response;
    }
    public String listMovieShowAvailability(String movieType){
        log = "List Shown Successful";
        Status = "Passed";
        String response;
        Map<String, MovieModel> events = allMovies.get(movieType);
        StringBuilder builder = new StringBuilder();
        builder.append(movieType + ": ");
        if (events.size() != 0) {
            for (MovieModel event :
                    events.values()) {
                builder.append(event.toString() + ", ");
            }
        }
        String otherServer1, otherServer2;
        if (serverID.equals("ATW")) {
            otherServer1 = sendUDPMessage(Outremont_Server_Port, "listMovieAvailability", "null", movieType, "null", 0);
            otherServer2 = sendUDPMessage(Verdun_Server_Port, "listMovieAvailability", "null", movieType, "null", 0);
        } else if (serverID.equals("OUT")) {
            otherServer1 = sendUDPMessage(Atwater_Server_Port, "listMovieAvailability", "null", movieType, "null", 0);
            otherServer2 = sendUDPMessage(Verdun_Server_Port, "listMovieAvailability", "null", movieType, "null", 0);
        } else {
            otherServer1 = sendUDPMessage(Atwater_Server_Port, "listMovieAvailability", "null", movieType, "null", 0);
            otherServer2 = sendUDPMessage(Outremont_Server_Port, "listMovieAvailability", "null", movieType, "null", 0);
        }
        builder.append(otherServer1).append(otherServer2);
        writeToLog("listMovieShowAvailability",movieType,Status, "List shown successfully for " + movieType);
        response = builder.toString().substring(0, builder.length()-2);
        return response;
    }
    public String bookMovieTickets(String customerID, String movieID, String movieType, int noOfTickets) {
        log = "Movie Booked Successful";
        Status = "Passed";
        String response;
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
        }
        if (MovieModel.detectMovieServer(movieID).equals(serverName)) {
            if(allMovies.get(movieType).containsKey(movieID)){
                MovieModel bookedEvent = allMovies.get(movieType).get(movieID);
                if (!bookedEvent.isFull(noOfTickets)) {
                    if (clientMovies.containsKey(customerID)) {
                        if (clientMovies.get(customerID).containsKey(movieType)) {
                            if (clientMovies.get(customerID).get(movieType).containsKey(movieID)) {
                                clientMovies.get(customerID).get(movieType).put(movieID, clientMovies.get(customerID).get(movieType).get(movieID) + noOfTickets);
                            } else {
                                clientMovies.get(customerID).get(movieType).put(movieID, noOfTickets);
                            }
                        } else {
                            Map<String, Integer> temp = new ConcurrentHashMap<>();
                            temp.put(movieID, noOfTickets);
                            clientMovies.get(customerID).put(movieType, temp);
                        }
                    } else {
                        Map<String, Map<String, Integer>> temp = new ConcurrentHashMap<>();
                        Map<String, Integer> temp2 = new ConcurrentHashMap<>();
                        temp2.put(movieID, noOfTickets);
                        temp.put(movieType, temp2);
                        clientMovies.put(customerID, temp);
                    }
                    if (allMovies.get(movieType).get(movieID).addRegisteredClientID(customerID, noOfTickets) == MovieModel.ADD_SUCCESS) {
                        writeToLog("bookMovieTickets",movieType + " "+ customerID + " " + movieID + " " + noOfTickets,Status, "Movie Ticket Booked Successfully ");
                        response = "Success: Movie " + movieID + " Booked Successfully";
                    } else if (allMovies.get(movieType).get(movieID).addRegisteredClientID(customerID, noOfTickets) == MovieModel.EVENT_FULL) {
                        log= "Movie can't be booked.";
                        Status = "Failed";
                        writeToLog("bookMovieTickets",movieType + " "+ customerID + " " + movieID + " " + noOfTickets,Status, "Movie Ticket cant be Booked");
                        response = "Failed: Movie " + movieID + " is Full";
                    } else {
                        log= "Movie can't be booked.";
                        Status = "Failed";
                        writeToLog("bookMovieTickets",movieType + " "+ customerID + " " + movieID + " " + noOfTickets,Status, "Movie Ticket cant be Booked");
                        response = "Failed: Cannot Book Movie= " + movieID;
                    }
/*                try {
                    Logger.serverLog(serverID, customerID, " RMI bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                } else {
                    log= "Movie can't be booked.";
                    Status = "Failed";
                    writeToLog("bookMovieTickets",movieType + " "+ customerID + " " + movieID + " " + noOfTickets,Status, "Movie Ticket cant be Booked");
                    response = "Failed: Not enough seats for " + movieID ;
                /*try {
                    Logger.serverLog(serverID, customerID, " RMI bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                }
            } else {
                log= "Movie can't be booked.";
                Status = "Failed";
                writeToLog("bookMovieTickets",movieType + " "+ customerID + " " + movieID + " " + noOfTickets,Status, "Movie Ticket cant be Booked");
                response = "Incorrect MovieID. No Movie show exists.";
            }
            return response;
        } else {
            if (!exceedWeeklyLimit(customerID, movieID.substring(4))) {
                if(!checkForSimilarMovieShows(customerID, movieID, movieType)) {
                    String serverResponse = sendUDPMessage(getServerPort(movieID.substring(0, 3)), "bookMovie", customerID, movieType, movieID, noOfTickets);
                    if (serverResponse.startsWith("Success:")) {
                        if (clientMovies.get(customerID).containsKey(movieType)) {
                            clientMovies.get(customerID).get(movieType).put(movieID, noOfTickets);
                        } else {
                            Map<String, Integer> temp = new ConcurrentHashMap<>();
                            temp.put(movieID, noOfTickets);
                            clientMovies.get(customerID).put(movieType, temp);
                        }
                    }
/*                try {
                    Logger.serverLog(serverID, customerID, " RMI bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                    return serverResponse;
                } else {
                    log= "Movie can't be booked.";
                    Status = "Failed";
                    writeToLog("bookMovieTickets",movieType + " "+ customerID + " " + movieID + " " + noOfTickets,Status, "Movie Ticket cant be Booked");
                    response = "Failed: You Cannot Book the tickets of the same movie for the same show";
                    return response;
                }
            } else {
                log= "Movie can't be booked.";
                Status = "Failed";
                writeToLog("bookMovieTickets",movieType + " "+ customerID + " " + movieID + " " + noOfTickets,Status, "Movie Ticket cant be Booked");
                response = "Failed: You Cannot Book Event in Other Servers For This Week(Max Weekly Limit = 3)";
/*                try {
                    Logger.serverLog(serverID, customerID, " RMI bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                return response;
            }
        }
    }
    public String getBookingSchedule(String customerID)  {
        log = "Booking Schedule Shown Successfully";
        Status = "Passed";
        String response;
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
            log= "Booking schedule cant be shown";
            Status = "Failed";
            writeToLog("getBookingSchedule",customerID ,Status, "Booking Schedule can't be shown");
            response = "Booking Schedule Empty For " + customerID;

            return response;
        }
        Map<String, Map<String, Integer>> events = clientMovies.get(customerID);
        if (events.size() == 0) {
            log= "Booking schedule cant be shown";
            Status = "Failed";
            writeToLog("getBookingSchedule",customerID ,Status, "Booking Schedule can't be shown");
            response = "Booking Schedule Empty For " + customerID;
            return response;
        }
        StringBuilder builder = new StringBuilder();
        for (String eventType :
                events.keySet()) {
            builder.append(eventType + ":\n");
            for (String eventID :
                    events.get(eventType).keySet()) {
                String key = eventID.toString();
                int value = events.get(eventType).get(key);
                builder.append(key + " : " + value + "\n");
            }
        }
        writeToLog("getBookingSchedule",customerID ,Status, "Booking Schedule Shown Successfully");
        response = builder.toString();
/*        try {
            Logger.serverLog(serverID, customerID, " RMI getBookingSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return response;
    }
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int noOfTickets)  {
        log = "Movie Ticket Cancelled";
        Status = "Passed";
        String response;
        if (MovieModel.detectMovieServer(movieID).equals(serverName)) {
            if (customerID.substring(0, 3).equals(serverID)) {
                if (!serverClients.containsKey(customerID)) {
                    addNewCustomerToClients(customerID);
                    log = "Movie Ticket cant be Cancelled";
                    Status = "Failed";
                    writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Can't be Cancelled");
                    response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                   /* try {
                        Logger.serverLog(serverID, customerID, " RMI cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                } else {
                    if (clientMovies.containsKey(customerID) && clientMovies.get(customerID).containsKey(movieName) && clientMovies.get(customerID).get(movieName).containsKey(movieID)) {
                        if(checkMovieBookingCancel(customerID, movieID, movieName, noOfTickets)){
                            if(allMovies.get(movieName).get(movieID).removeRegisteredClientBooking(customerID, noOfTickets)){
                                writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Cancelled");
                                response = "Success: Movie " + movieID + " Canceled for " + customerID;
                            } else {
                                log = "Movie Ticket cant be Cancelled";
                                Status = "Failed";
                                writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Can't be Cancelled");
                                response = "Number of Tickets to cancel is more than booked tickets";
                            }
                        } else {
                            log = "Movie Ticket cant be Cancelled";
                            Status = "Failed";
                            writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Can't be Cancelled");
                            response = "Number of Tickets to cancel is more than booked tickets";
                        }
                        /*try {
                            Logger.serverLog(serverID, customerID, " RMI cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    } else {
                        log = "Movie Ticket cant be Cancelled";
                        Status = "Failed";
                        writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Can't be Cancelled");
                        response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                        /*try {
                            Logger.serverLog(serverID, customerID, " RMI cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }
                }
            } else {
                if (allMovies.get(movieName).get(movieID).removeRegisteredClientBooking(customerID, noOfTickets)) {
                    writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Cancelled");
                    response = "Success: Event " + movieID + " Canceled for " + customerID;
                } else {
                    log = "Movie Ticket cant be Cancelled";
                    Status = "Failed";
                    writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Can't be Cancelled");
                    response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                }
            }
            return response;
        } else {
            if (customerID.substring(0, 3).equals(serverID)) {
                if (!serverClients.containsKey(customerID)) {
                    addNewCustomerToClients(customerID);
                } else {
                    if (clientMovies.containsKey(customerID) && clientMovies.get(customerID).containsKey(movieName) && clientMovies.get(customerID).get(movieName).containsKey(movieID)) {
                        if(checkMovieBookingCancel(customerID, movieID, movieName, noOfTickets)){
                            return sendUDPMessage(getServerPort(movieID.substring(0, 3)), "cancelMovie", customerID, movieName, movieID, noOfTickets);
                        } else {
                            log = "Movie Ticket cant be Cancelled";
                            Status = "Failed";
                            writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Can't be Cancelled");
                            return "Number of Tickets to cancel is more than booked tickets";
                        }
                    }
                }
            }
            log = "Movie Ticket cant be Cancelled";
            Status = "Failed";
            writeToLog("cancelMovieTickets",customerID + " " + movieID + " " + movieName ,Status, "Movie Tickets Can't be Cancelled");
            return "Failed: You " + customerID + " Are Not Registered in " + movieID;
        }
    }

    public String exchangeTickets(String customerID, String old_movieID, String new_movieID, String new_movieName, String old_movieName, int noOfTickets)  {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Failed: You " + customerID + " Are Not Registered in " + old_movieID;
        } else {
            if (clientHasMovie(customerID, old_movieName, old_movieID)) {
                String bookResp = "Failed: did not send book request for your newMovie " + new_movieID;
                String cancelResp = "Failed: did not send cancel request for your oldMovie " + old_movieID;
                synchronized (this) {
                    if (onTheSameWeek(new_movieID.substring(4), old_movieID) && !exceedWeeklyLimit(customerID, new_movieID.substring(4))) {
                        cancelResp = cancelMovieTickets(customerID, old_movieID, old_movieName, noOfTickets);
                        if (cancelResp.startsWith("Success:")) {
                            bookResp = bookMovieTickets(customerID, new_movieID, new_movieName, noOfTickets);
                        }
                    } else {
                        bookResp = bookMovieTickets(customerID, new_movieID, new_movieName, noOfTickets);
                        if (bookResp.startsWith("Success:")) {
                            cancelResp = cancelMovieTickets(customerID, old_movieID, old_movieName, noOfTickets);
                        }
                    }
                }
                if (bookResp.startsWith("Success:") && cancelResp.startsWith("Success:")) {
                    response = "Success: Event " + old_movieID + " swapped with " + new_movieID;
                } else if (bookResp.startsWith("Success:") && cancelResp.startsWith("Failed:")) {
                    cancelMovieTickets(customerID, new_movieID, new_movieName, noOfTickets);
                    response = "Failed: Your oldEvent " + old_movieID + " Could not be Canceled reason: " + cancelResp;
                } else if (bookResp.startsWith("Failed:") && cancelResp.startsWith("Success:")) {
                    //hope this won't happen, but just in case.
                    String resp1 = bookMovieTickets(customerID, old_movieID, old_movieName, noOfTickets);
                    response = "Failed: Your newEvent " + new_movieID + " Could not be Booked reason: " + bookResp + " And your old event Rolling back: " + resp1;
                } else {
                    response = "Failed: on Both newEvent " + new_movieID + " Booking reason: " + bookResp + " and oldEvent " + old_movieID + " Canceling reason: " + cancelResp;
                }
            } else {
                response = "Failed: You " + customerID + " Are Not Registered in " + old_movieID;
            }
        }
        return response;
    }


    //--------------------------- UDP RELATED FUNCTIONS ------------------------------------------------

    private String sendUDPMessage(int serverPort, String method, String customerID, String movieType, String movieId, int noOfTickets) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieType + ";" + movieId + ";" + noOfTickets;
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

    private String getNextSameEvent(Set<String> keySet, String movieType, String oldMovieID, int noOfTickets) {
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
            if (!allMovies.get(movieType).get(sortedIDs.get(i)).isFull(noOfTickets)) {
                return sortedIDs.get(i);
            }
        }
        return "Failed";
    }

    private boolean exceedWeeklyLimit(String customerID, String movieDate) {
        int limit = 0;
        for (int i = 0; i < 3; i++) {
            List<String> registeredIDs = new ArrayList<>();
            // Map<String, Integer> registeredIDs = new ConcurrentHashMap<>();
            switch (i) {
                case 0:
                    if (clientMovies.get(customerID).containsKey(MovieModel.AVATAR)) {
                        Set<String> keySet = clientMovies.get(customerID).get(MovieModel.AVATAR).keySet();
                        registeredIDs = new ArrayList<String>(keySet);
                    }
                    break;
                case 1:
                    if (clientMovies.get(customerID).containsKey(MovieModel.AVENGERS)) {
                        Set<String> keySet = clientMovies.get(customerID).get(MovieModel.AVENGERS).keySet();
                        registeredIDs = new ArrayList<String>(keySet);
                    }
                    break;
                case 2:
                    if (clientMovies.get(customerID).containsKey(MovieModel.TITANIC)) {
                        Set<String> keySet = clientMovies.get(customerID).get(MovieModel.TITANIC).keySet();
                        registeredIDs = new ArrayList<String>(keySet);
                    }
                    break;
            }
            for (String eventID :
                    registeredIDs) {
                if (eventID.substring(6, 8).equals(movieDate.substring(2, 4)) && eventID.substring(8, 10).equals(movieDate.substring(4, 6)) && !(customerID.substring(0, 3).equals(eventID.substring(0, 3)))) {
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

    private boolean checkForSimilarMovieShows(String customerID, String movieId, String movieType) {
        List<String> registeredIDs = new ArrayList<>();
        if(clientMovies.get(customerID).containsKey(movieType)) {
            if(!clientMovies.get(customerID).get(movieType).containsKey(movieId)) {
                Set<String> keySet = clientMovies.get(customerID).get(movieType).keySet();
                registeredIDs = new ArrayList<String>(keySet);
                for (String eventID :
                        registeredIDs) {
                    if(eventID.substring(3, 10).equals(movieId.substring(3, 10))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addCustomersToNextSameEvent(String oldMovieID, String movieType, List<String> registeredClients) {
        for (String customerID :
                registeredClients) {
            if (customerID.substring(0, 3).equals(serverID)) {
                int noOfTickets = clientMovies.get(customerID).get(movieType).get(oldMovieID);
                clientMovies.get(customerID).get(movieType).remove(oldMovieID);
                String nextSameEventResult = getNextSameEvent(allMovies.get(movieType).keySet(), movieType, oldMovieID, noOfTickets);
                if (nextSameEventResult.equals("Failed")) {
                    return;
                } else {

                     bookMovieTickets(customerID, nextSameEventResult, movieType, noOfTickets);
                }
            } else {
                sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeMovie", customerID, movieType, oldMovieID, 0);
            }
        }
    }

    private boolean checkMovieBookingCancel(String customerId, String movieId, String movieName, int numberOfTickets){
        if(clientMovies.get(customerId).get(movieName).get(movieId) >= numberOfTickets) {
            clientMovies.get(customerId).get(movieName).put(movieId, clientMovies.get(customerId).get(movieName).get(movieId) - numberOfTickets);
            if(clientMovies.get(customerId).get(movieName).get(movieId) == 0) {
                clientMovies.get(customerId).get(movieName).remove(movieId);
            }
            return true;
        }
        return false;
    }

    private synchronized boolean checkClientExists(String customerID) {
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
            return false;
        } else {
            return true;
        }
    }

    private boolean onTheSameWeek(String newMovieDate, String movieID) {
        if (movieID.substring(6, 8).equals(newMovieDate.substring(2, 4)) && movieID.substring(8, 10).equals(newMovieDate.substring(4, 6))) {
            int week1 = Integer.parseInt(movieID.substring(4, 6)) / 7;
            int week2 = Integer.parseInt(newMovieDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(day2 - day1);
            return week1 == week2;
        } else {
            return false;
        }
    }

    private boolean isWithinAWeek(String movieID) {

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        LocalDate date = LocalDate.parse(movieID.substring(4, 10), formatter);
        long daysBetween = ChronoUnit.DAYS.between(now, date);
        return (daysBetween <= 7 && daysBetween >= 0);
    }

    private synchronized boolean clientHasMovie(String customerID, String movieType, String movieId) {
        if (clientMovies.get(customerID).containsKey(movieType)) {
            return clientMovies.get(customerID).get(movieType).containsKey(movieId);
        } else {
            return false;
        }
    }

    //--------------------------- UDP Specific functions (ONLY FOR UDP CALLS)---------------------------

    public String removeMovieUDP(String oldEventID, String eventType, String customerID) {
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
            return "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
        } else {
            if (clientMovies.get(customerID).get(eventType).containsKey(oldEventID)) {
                clientMovies.get(customerID).get(eventType).remove(oldEventID);
                return "Success: Event " + oldEventID + " Was Removed from " + customerID + " Schedule";
            } else {
                return "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
            }
        }
    }

    public String listMovieAvailabilityUDP(String eventType) {
        Map<String, MovieModel> events = allMovies.get(eventType);
        StringBuilder builder = new StringBuilder();
        if (events.size() != 0) {
            for (MovieModel event :
                    events.values()) {
                builder.append(event.toString() + ", ");
            }
        }
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
        System.out.println("server clients : " + serverClients);
        System.out.println("client Movies : " + clientMovies);
    }

    //--------------------------HASH MAP GETTERS--------------------------------------------

    public Map<String, Map<String, MovieModel>> getAllMovies() {
        return allMovies;
    }

    public Map<String, Map<String, Map<String, Integer>>> getClientMovies() {
        return clientMovies;
    }

    public Map<String, ClientModel> getServerClients() { return serverClients; }

    // ------------------------- Implementing Logger -----------------------

    public void writeToLog(String operation, String params, String status, String responceDetails) {
        try {
            FileWriter myWriter = new FileWriter("D:\\MOVIE_TICKET_BOOKING - CORBA\\movie_ticket_booking_system_design\\src\\Logs\\" + file.get(this.serverName), true);
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String log = dateFormat.format(LocalDateTime.now()) + " : " + operation + " : " + params + " : " + status
                    + " : " + responceDetails + "\n";
            myWriter.write(log);
            myWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
