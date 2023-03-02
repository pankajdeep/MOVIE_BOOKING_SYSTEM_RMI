package movieTicketBookingInterfaceApp;


/**
* movieTicketBookingInterfaceApp/movieTicketBookingInterfaceOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from movieTicketBookingInterface.idl
* Friday, 24 February, 2023 7:05:33 PM EST
*/

public interface movieTicketBookingInterfaceOperations 
{
  String addMovieSlots (String movieID, String movieName, int bookingCapacity);
  String removeMovieSlots (String movieID, String movieName);
  String listMovieShowAvailability (String movieName);
  String cancelMovieTickets (String customerID, String movieID, String movieName, int noOfTickets);
  String bookMovieTickets (String customerID, String movieID, String movieName, int noOfTickets);
  String getBookingSchedule (String customerID);
  String exchangeTickets (String customerID, String old_movieID, String new_movieID, String new_movieName, String old_movieName, int numberOfTickets);
  void shutdown ();
} // interface movieTicketBookingInterfaceOperations