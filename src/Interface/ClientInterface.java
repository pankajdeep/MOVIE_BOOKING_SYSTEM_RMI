package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    /**
     * Add New Movie Slots
     * @param movieID
     * @param movieName
     * @param bookingCapacity
     * @return
     */
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws RemoteException;

    /**
     * Remove Movie Slots
     * @param movieID
     * @param movieName
     * @return
     */
    public String removeMovieSlots(String movieID, String movieName) throws RemoteException;

    /**
     * List all the no of tickets
     * Available for a particular
     * movie in all locations
     * @param movieName
     * @return
     */
    public String listMovieShowAvailability(String movieName) throws RemoteException;

    /**
     * Book Movie Tickets
     * @param customerID
     * @param movieID
     * @param movieName
     * @param noOfTickets
     * @return
     */
    public String bookMovieTickets(String customerID, String movieID, String movieName, int noOfTickets) throws RemoteException;

    /**
     * Get list of all bookings
     * by the user
     * @param customerID
     * @return
     */
    public String getBookingSchedule(String customerID) throws RemoteException;

    /**
     * Cancel any tickets booked
     * by the user
     * @param customerID
     * @param movieID
     * @param movieName
     * @param noOfTickets
     * @return
     */
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int noOfTickets) throws RemoteException;
}
