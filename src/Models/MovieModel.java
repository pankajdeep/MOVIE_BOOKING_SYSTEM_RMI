package Models;

import java.util.ArrayList;
import java.util.List;

public class MovieModel {
    public static final String MOVIE_TIME_MORNING = "Morning";
    public static final String MOVIE_TIME_AFTERNOON = "Afternoon";
    public static final String MOVIE_TIME_EVENING = "Evening";
    public static final String AVATAR = "Avatar";
    public static final String AVENGERS = "Avengers";
    public static final String TITANIC = "Titanic";
    public static final String MOVIE_SERVER_ATWATER = "Atwater";
    public static final String MOVIE_SERVER_OUTREMONT = "Outremont";
    public static final String MOVIE_SERVER_VERDUN = "Verdun";
    private String movieType;
    private String movieID;
    private String movieServer;
    private int movieCapacity;
    private String movieDate;
    private String movieTimeSlot;
    private List<String> registeredClients;
    public static final int EVENT_FULL = -1;
    public static final int ALREADY_REGISTERED = 0;
    public static final int ADD_SUCCESS = 1;

    public MovieModel(String movieType, String movieID, int movieCapacity) {
        this.movieID = movieID;
        this.movieType = movieType;
        this.movieCapacity = movieCapacity;
        this.movieTimeSlot = detectMovieTimeSlot(movieID);
        this.movieServer = detectMovieServer(movieID);
        this.movieDate = detectMovieDate(movieID);
        registeredClients = new ArrayList<>();
    }

    public static String detectMovieServer(String eventID) {
        if (eventID.substring(0, 3).equalsIgnoreCase("ATW")) {
            return MOVIE_SERVER_ATWATER;
        } else if (eventID.substring(0, 3).equalsIgnoreCase("OUT")) {
            return MOVIE_SERVER_OUTREMONT;
        } else {
            return MOVIE_SERVER_VERDUN;
        }
    }

    public static String detectMovieTimeSlot(String eventID) {
        if (eventID.substring(3, 4).equalsIgnoreCase("M")) {
            return MOVIE_TIME_MORNING;
        } else if (eventID.substring(3, 4).equalsIgnoreCase("A")) {
            return MOVIE_TIME_AFTERNOON;
        } else {
            return MOVIE_TIME_EVENING;
        }
    }

    public static String detectMovieDate(String eventID) {
        return eventID.substring(4, 6) + "/" + eventID.substring(6, 8) + "/20" + eventID.substring(8, 10);
    }

    public String getMovieType() {
        return movieType;
    }
    public String getMovieID() {
        return movieID;
    }
    public String getMovieServer() { return movieServer; }
    public int getMovieCapacity() {
        return movieCapacity;
    }
    public void setMovieCapacity(int movieCapacity) {
        this.movieCapacity = movieCapacity;
    }
    public int getMovieRemainCapacity() {
        return movieCapacity - registeredClients.size();
    }
    public String getMovieDate() {
        return movieDate;
    }
    public String getMovieTimeSlot() {
        return movieTimeSlot;
    }
    public boolean isFull() {
        return getMovieCapacity() == registeredClients.size();
    }

    public List<String> getRegisteredClientIDs() {
        return registeredClients;
    }

    public int addRegisteredClientID(String registeredClientID) {
        if (!isFull()) {
            if (registeredClients.contains(registeredClientID)) {
                return ALREADY_REGISTERED;
            } else {
                registeredClients.add(registeredClientID);
                return ADD_SUCCESS;
            }
        } else {
            return EVENT_FULL;
        }
    }

    public boolean removeRegisteredClientID(String registeredClientID) {
        return registeredClients.remove(registeredClientID);
    }

    @Override
    public String toString() {
        return " (" + getMovieID() + ") in the " + getMovieTimeSlot() + " of " + getMovieDate() + " Total[Remaining] Capacity: " + getMovieCapacity() + "[" + getMovieRemainCapacity() + "]";
    }
}
