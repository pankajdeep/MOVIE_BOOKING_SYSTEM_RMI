package Client;

import Interface.ClientInterface;

import java.io.FileWriter;
import java.io.IOException;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import static Constants.Constants.*;


public class Client {
    public static String userSession;
    public static String log;
    private static ClientInterface movieObj;
    public static Service atwaterService;
    public static Service outremontService;
    public static Service verdunService;
    public static void main(String[] args) throws Exception {
        URL atwaterURL = new URL("http://localhost:8080/atwater?wsdl");
        QName atwaterQName = new QName("http://MethodImplementation/", "MethodImplementationService");
        atwaterService = Service.create(atwaterURL, atwaterQName);

        URL outremontURL = new URL("http://localhost:8081/outremont?wsdl");
        QName outremontQName = new QName("http://MethodImplementation/", "MethodImplementationService");
        outremontService = Service.create(outremontURL, outremontQName);

        URL verdunURL = new URL("http://localhost:8082/verdun?wsdl");
        QName verdunQName = new QName("http://MethodImplementation/", "MethodImplementationService");
        verdunService = Service.create(verdunURL, verdunQName);

        startMain();
    }
    public static void startMain() throws Exception {

        Scanner input = new Scanner(System.in);
        System.out.println("Please enter user id: ");
        String userID = input.nextLine();
        userSession = userID;
        String clientType = checkClientType(userID);
        String serverPort = getServerPort(userID.substring(0,3));
        if(clientType == "Admin"){
            //logger file
            if(serverPort == "false"){
                return;
            }
//            ClientInterface adminObj = (ClientInterface) Naming.lookup(serverPort);
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
                        log="Add Movie Slots";
                        serverResponse = movieObj.addMovieSlots(movieID, movieName, movieCapacity);
                        writeToLogFile("addMovieSlots",userID+" "+movieID+" "+movieName+" "+movieCapacity,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 2:
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        log="Remove Movie Slots";
                        serverResponse = movieObj.removeMovieSlots(movieID, movieName);
                        writeToLogFile("removeMovieSlots",userID+" "+movieID+" "+movieName,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 3:
                        movieName = promptForMovieType();
                        log="List Movie Shows";
                        serverResponse = movieObj.listMovieShowAvailability(movieName);
                        writeToLogFile("listMovieShowAvailability",userID+" "+" "+movieName,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 4:
                        customerID = askForCustomerIDFromAdmin(userID.substring(0, 3));
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        log="Book Movie Tickets";
                        serverResponse = movieObj.bookMovieTickets(customerID, movieID, movieName, ticketCount);
                        writeToLogFile("bookMovieTickets",userID+" "+" "+movieName,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 5:
                        customerID = askForCustomerIDFromAdmin(userID.substring(0, 3));
                        log="Get Booking Schedule";
                        serverResponse = movieObj.getBookingSchedule(customerID);
                        writeToLogFile("getBookingSchedule",userID,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 6:
                        customerID = askForCustomerIDFromAdmin(userID.substring(0, 3));
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        log="Cancel Movie Tickets";
                        serverResponse = movieObj.cancelMovieTickets(customerID, movieID, movieName, ticketCount);
                        writeToLogFile("cancelMovieTickets",userID,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 7:
                        customerID = askForCustomerIDFromAdmin(userID.substring(0, 3));
                        System.out.println("Please Enter the OLD movie name to be replaced");
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        System.out.println("Please Enter the NEW Movie name to be replaced");
                        String newMovieName = promptForMovieType();
                        String newMovieId = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        serverResponse = movieObj.exchangeTickets(customerID, movieID ,newMovieId, newMovieName, movieName, ticketCount);
                        System.out.println(serverResponse);
                        break;
                    case 8:
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
//            ClientInterface customerObj = (ClientInterface) Naming.lookup(serverPort);
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
                        log="Book Movie Tickets";
                        serverResponse = movieObj.bookMovieTickets(userID, movieID, movieName, ticketCount);
                        writeToLogFile("bookMovieTickets",userID+" "+" "+movieName,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 2:
                        log="Get Booking Schedule";
                        serverResponse = movieObj.getBookingSchedule(userID);
                        writeToLogFile("getBookingSchedule",userID,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 3:
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        log="Cancel Movie Tickets";
                        serverResponse = movieObj.cancelMovieTickets(userID, movieID, movieName, ticketCount);
                        writeToLogFile("cancelMovieTickets",userID,serverResponse);
                        System.out.println(serverResponse);
                        //logger file
                        break;
                    case 4:
                        System.out.println("Please Enter the OLD movie name to be replaced");
                        movieName = promptForMovieType();
                        movieID = promptForMovieID();
                        System.out.println("Please Enter the NEW Movie name to be replaced");
                        String newMovieName = promptForMovieType();
                        String newMovieId = promptForMovieID();
                        ticketCount = promptForTotalTicket();
                        serverResponse = movieObj.exchangeTickets(userID, movieID ,newMovieId, newMovieName, movieName, ticketCount);
                        System.out.println(serverResponse);
                        break;
                    case 5:
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
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a customerID(Within " + adminID + " Server):");
        String userID = input.next().trim().toUpperCase();
        if (checkClientType(userID) != "Customer" || !userID.substring(0, 3).equals(adminID)) {
            return askForCustomerIDFromAdmin(adminID);
        } else {
            return userID;
        }
    }

    private static int promptForMovieCapacity() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter the booking capacity:");
        return input.nextInt();
    }

    private static int promptForTotalTicket() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter the number of tickets:");
        return input.nextInt();
    }

    private static String promptForMovieID() {
        Scanner input = new Scanner(System.in);
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
        Scanner input = new Scanner(System.in);
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
            movieObj = atwaterService.getPort(ClientInterface.class);
            return "atwater";
        } else if (server.equalsIgnoreCase("OUT")) {
            movieObj = outremontService.getPort(ClientInterface.class);
            return "outremont";
        } else if (server.equalsIgnoreCase("VER")) {
            movieObj = verdunService.getPort(ClientInterface.class);
            return "verdun";
        }
        return "false";
    }

    private static void printMenu(String userType) {
        System.out.println("Please choose an option below:");
        if (userType == "Customer") {
            System.out.println("1. Book Movie Ticket");
            System.out.println("2. Get Booking Schedule");
            System.out.println("3. Cancel Movie Tickets");
            System.out.println("4. Swap Tickets");
            System.out.println("5. Logout");
        } else if (userType == "Admin") {
            System.out.println("1. Add Movie Slots");
            System.out.println("2. Remove Movie Slots");
            System.out.println("3. List Movie Shows Availability");
            System.out.println("4. Book Movie Ticket");
            System.out.println("5. Get Booking Schedule");
            System.out.println("6. Cancel Movie Tickets");
            System.out.println("7. Swap Tickets");
            System.out.println("8. Logout");
        }
    }

    public static void writeToLogFile(String operation, String params, String responceDetails) {
        try {
            FileWriter myWriter = new FileWriter("D:\\MOVIE_TICKET_BOOKING - WebServices\\movie_ticket_booking_system_design\\src\\Logs\\" + userSession + ".txt", true);
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String log = dateFormat.format(LocalDateTime.now()) + " : " + operation + " : " + params + " : "
                    + " : " + responceDetails + "\n";
            myWriter.write(log);
            myWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
