package Models;

public class ClientModel {
    public static final String CLIENT_TYPE_ADMIN = "Admin";
    public static final String CLIENT_TYPE_CUSTOMER = "Customer";
    public static final String CLIENT_SERVER_ATWATER = "Atwater";
    public static final String CLIENT_SERVER_OUTREMONT = "Outremont";
    public static final String CLIENT_SERVER_VERDUN = "Verdun";
    private String clientType;
    private String clientID;
    private String clientServer;

    public ClientModel(String clientID) {
        this.clientID = clientID;
        this.clientType = detectClientType();
        this.clientServer = detectClientServer();
    }

    private String detectClientServer() {
        if (clientID.substring(0, 3).equalsIgnoreCase("ATW")) {
            return CLIENT_SERVER_ATWATER;
        } else if (clientID.substring(0, 3).equalsIgnoreCase("OUT")) {
            return CLIENT_SERVER_OUTREMONT;
        } else {
            return CLIENT_SERVER_VERDUN;
        }
    }

    private String detectClientType() {
        if (clientID.substring(3, 4).equalsIgnoreCase("A")) {
            return CLIENT_TYPE_ADMIN;
        } else {
            return CLIENT_TYPE_CUSTOMER;
        }
    }

    public String getClientType() {
        return clientType;
    }
    public String getClientID() {
        return clientID;
    }
    public String getClientServer() {
        return clientServer;
    }

    @Override
    public String toString() {
        return getClientType() + "(" + getClientID() + ") on " + getClientServer() + " Server.";
    }
}
