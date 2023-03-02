package Constants;

public class Constants {
    private static final String SERVER_ATWATER = "rmi://localhost/atw";
    private static final String SERVER_OUTREMONT = "rmi://localhost/out";
    private static final String SERVER_VERDUN = "rmi://localhost/ver";

    public static String getAtwaterServer() {
        return SERVER_ATWATER;
    }

    public static String getOutremontServer() {
        return SERVER_OUTREMONT;
    }

    public static String getVerdunServer() {
        return SERVER_VERDUN;
    }
}