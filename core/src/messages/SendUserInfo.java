package messages;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import beans.EncryptedUser;
import beans.MyInfo;
import command.Command;
import command.SendUserInfoCommand;
import tools.Constants;
import tools.IOStreams;
import tools.Tools;

public class SendUserInfo {
	public static void sendUserInfo(MyInfo myInfo, List<EncryptedUser> EncryptedUsers){

		try {
			
			// socket connection to the RS
			Socket socketServer = new Socket("127.0.0.1", 6666);
			IOStreams streamServer = new IOStreams(socketServer);
			
			Command sendUserInfoCommand = new SendUserInfoCommand(Constants.CMD_SEND_User_INFO, myInfo, EncryptedUsers);
			streamServer.getOutputStream().writeObject(sendUserInfoCommand);
			int statusServer = streamServer.getInputStream().readInt();
			Tools.interpretStatusServer( statusServer );

			streamServer.close();
			socketServer.close();
									
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Error sendUserInfo to the RS ..");
		} 
	}
}
