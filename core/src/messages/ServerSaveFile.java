package messages;

import java.net.Socket;

import beans.User;
import command.ServerSaveFileCommand;
import tools.Constants;
import tools.IOStreams;
import tools.Tools;

public class ServerSaveFile {
	public static void serverSaveFile(String file, String fileName, String skey, String iv, User user, String command){

		/**
		 * Establishes the connection with the TCELL and send to file with the store message.
		 * @param fileToSendPath the path of the file to send
		 * @param user the addressee user
		 */
		try {
			/* The socket used to send the file and the messages */
			Socket socket;

			/* Extract the fileName from the path */
			//String fileName = Tools.getFileName(fileToSendPath);

			System.out.println("File is being sent to TCell...");

			/* Creates a stream socket and connects it to the specified port number at the specified IP address */
			socket = new Socket("127.0.0.1", 6666);

			/* Creation of the stream */
			IOStreams stream = new IOStreams( socket );
			
			int userID = user.getUserGID();
			String gid = userID + "|" + fileName;

			ServerSaveFileCommand serverSaveFileCmd= new ServerSaveFileCommand(Constants.CMD_SERVER_SAVE_FILE, file, gid, skey, iv, userID, command);

			//send command
			stream.getOutputStream().writeObject(serverSaveFileCmd);
			
			//recieve status from the server
			int status = stream.getInputStream().readInt();
			Tools.interpretStatus( status );

			stream.close();
			socket.close();

		} catch (Exception ex) {
			System.err.println("ERROR : store file has failed");
		}
	}
}
