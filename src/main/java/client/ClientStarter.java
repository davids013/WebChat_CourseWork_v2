package client;

public class ClientStarter {
    public static void main(String[] args) {
        Thread client = new Thread(WebClient::start);
        client.start();
    }
}