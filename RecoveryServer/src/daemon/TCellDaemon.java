package daemon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Daemon in TrustedCell.
 * 
 * @author Athanasia Katsouraki
 */
public class TCellDaemon{


	public static void main(String[] args) throws IOException {

		ServerSocket server = null;
		try {

			/* Creation of the server socket */
			server = new ServerSocket(6666);

			/* The server listens for new connections and accepts it */
			System.out.println("RS Daemon started...");

			while (true) {
				System.out.println("\nWaiting for a connection from an APP or from other TCells");
				Socket clientSocket = server.accept();
				System.out.println("Accepted connection : " + clientSocket);

				/* For each socket, a new thread is created */
				ClientConnectionManager ccm = new ClientConnectionManager(clientSocket);
				ccm.start();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			server.close();
		}
	}
}