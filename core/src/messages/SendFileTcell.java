package messages;

import java.net.Socket;

import beans.FileStored;
import beans.User;
import command.Command;
import command.SendFileTcellCommand;
import tools.Constants;
import tools.IOStreams;
import tools.Tools;

public class SendFileTcell {

	public static void send(User user, FileStored fileStored){

		try {
			Socket socket = new Socket(user.getTCellIP(), user.getPort());
			IOStreams stream = new IOStreams(socket);
			
			Command sendFileCommand = new SendFileTcellCommand(Constants.CMD_SEND_FILE_TCELL, fileStored);
			stream.getOutputStream().writeObject(sendFileCommand);
			int status = stream.getInputStream().readInt();
			Tools.interpretStatus( status );

			stream.close();
			socket.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("recipient TCell is not connected! The file can not be sent!");
		}

	}
}
