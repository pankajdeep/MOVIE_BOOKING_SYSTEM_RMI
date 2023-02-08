package Client;

import Interface.ClientInterface;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Scanner;

import static Constants.Constants.*;


public class Client {
    static Scanner input = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        startMain();
    }
    public static void startMain() throws IOException, NotBoundException {
        System.out.println("Please enter user id: ");
        String userID = input.nextLine();

        String clientType = checkClientType(userID);
        String serverPort = getServerPort(userID.substring(0,3));

        if(clientType == "Admin"){
            //logger file
            if(serverPort == "false"){
                return;
            }
            ClientInterface adminObj = (ClientInterface) Naming.lookup(serverPort);
            while (true) {
                String customerID;
                String movieName;
                String movieID;
                String serverResponse;
                int movieCapacity;
                int ticketCount;
                printMenu("Admin");
                int option = input.nextInt();
                switch (option) {
                    case 1:
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        movieCapacity = promptForMovieCapacity();
                        serverResponse = adminObj.addMovieSlots(movieID, movieName, movieCapacity);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 2:
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        serverResponse = adminObj.removeMovieSlots(movieID, movieName);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 3:
                        movieName = promptForMovieType();
                        serverResponse = adminObj.listMovieShowAvailability(movieName);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 4:
                        customerID = askForCustomerIDFromAdmin(userID.substring(0, 3));
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        serverResponse = adminObj.bookMovieTickets(customerID, movieID, movieName, ticketCount);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 5:
                        customerID = askForCustomerIDFromAdmin(userID.substring(0, 3));
                        serverResponse = adminObj.getBookingSchedule(customerID);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 6:
                        customerID = askForCustomerIDFromAdmin(userID.substring(0, 3));
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        serverResponse = adminObj.cancelMovieTickets(customerID, movieID, movieName, ticketCount);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 7:
                        //logger file
                        startMain();
                        break;
                }
            }

        }
        else if(clientType == "Customer") {
            //logger file
            if(serverPort == "false"){
                return;
            }
            ClientInterface customerObj = (ClientInterface) Naming.lookup(serverPort);
            while (true){
                String movieName;
                String movieID;
                String serverResponse;
                int ticketCount;
                printMenu("Customer");
                int option = input.nextInt();
                switch (option) {
                    case 1:
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        serverResponse = customerObj.bookMovieTickets(userID, movieID, movieName, ticketCount);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 2:
                        serverResponse = customerObj.getBookingSchedule(userID);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 3:
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        serverResponse = customerObj.cancelMovieTickets(userID, movieID, movieName, ticketCount);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 4:
                        //logger file
                        startMain();
                        break;
                }
            }
        }
        else {
            System.out.println("Incorrect UserID");
            // Implement Logger
            startMain();
        }


    }

    private static String askForCustomerIDFromAdmin(String adminID) {
        System.out.println("Please enter a customerID(Within " + adminID + " Server):");
        String userID = input.next().trim().toUpperCase();
        if (checkClientType(userID) != "Customer" || !userID.substring(0, 3).equals(adminID)) {
            return askForCustomerIDFromAdmin(adminID);
        } else {
            return userID;
        }
    }

    private static int promptForMovieCapacity() {
        System.out.println("Please enter the booking capacity:");
        return input.nextInt();
    }

    private static int promptForTotalTicket() {
        System.out.println("Please enter the number of tickets:");
        return input.nextInt();
    }

    private static String promptForMovieID() {
        System.out.println("Please enter the Movie ID (e.g ATWM190223)");
        String movieID = input.next().trim().toUpperCase();
        if (movieID.length() == 10) {
            if (movieID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    movieID.substring(0, 3).equalsIgnoreCase("OUT") ||
                    movieID.substring(0, 3).equalsIgnoreCase("VER")) {
                if (movieID.substring(3, 4).equalsIgnoreCase("M") ||
                        movieID.substring(3, 4).equalsIgnoreCase("A") ||
                        movieID.substring(3, 4).equalsIgnoreCase("E")) {
                    return movieID;
                }
            }
        }
        return promptForMovieID();
    }

    private static String promptForMovieType() {
        System.out.println("Please choose a Movie Name:");
        System.out.println("1. Avatar");
        System.out.println("2. Avengers");
        System.out.println("3. Titanic");
        switch (input.nextInt()) {
            case 1:
                return "Avatar";
            case 2:
                return "Avengers";
            case 3:
                return "Titanic";
        }
        return promptForMovieType();
    }

    private static String checkClientType(String userID) {
        if (userID.length() == 8 && (userID.substring(0, 3).equalsIgnoreCase("ATW") ||
                userID.substring(0, 3).equalsIgnoreCase("OUT") ||
                userID.substring(0, 3).equalsIgnoreCase("VER"))) {
            if (userID.charAt(3) == 'A') {
                return "Admin";
            } else if (userID.charAt(3) == 'C') {
                return "Customer";
            }
        }
        return "false";
    }

    private static String getServerPort(String server) {
        if (server.equalsIgnoreCase("ATW")) {
            return getAtwaterServer();
        } else if (server.equalsIgnoreCase("OUT")) {
            return getOutremontServer();
        } else if (server.equalsIgnoreCase("VER")) {
            return getVerdunServer();
        }
        return "false";
    }

    private static void printMenu(String userType) {
        System.out.println("Please choose an option below:");
        if (userType == "Customer") {
            System.out.println("1. Book Movie Ticket");
            System.out.println("2. Get Booking Schedule");
            System.out.println("3. Cancel Movie Tickets");
            System.out.println("4. Logout");
        } else if (userType == "Admin") {
            System.out.println("1. Add Movie Slots");
            System.out.println("2. Remove Movie Slots");
            System.out.println("3. List Movie Shows Availability");
            System.out.println("4. Book Movie Ticket");
            System.out.println("5. Get Booking Schedule");
            System.out.println("6. Cancel Movie Tickets");
            System.out.println("7. Logout");
        }
    }
}
