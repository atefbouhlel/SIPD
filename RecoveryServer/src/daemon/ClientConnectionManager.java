/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package daemon;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import beans.EncryptedUser;
import beans.FileDesc;
import beans.FileStored;
import beans.Info;
import beans.MyInfo;
import command.Command;
import command.GetFileToShareCommand;
import command.RecoverFilesCommand;
import command.SendUserInfoCommand;
import command.ServerSaveFileCommand;
import dao.RsDAO;
import tools.Constants;
import tools.IOStreams;
import tools.Tools;


/**
 * ThreadServer in RS. It receives commands from clients and executes the
 * associated actions.
 *
 * @author BOUHLEL Atef, SALAH Ghassen, ZAMMIT CHATTI Sarra
 *
 */
public class ClientConnectionManager extends Thread {
	Socket socket;

	public ClientConnectionManager(Socket s) {
		/**
		 * Creates a ThreadServer instance
		 * @param socket the client socket
		 */
		this.socket = s;
	}

	@Override
	public void run() {
		try {
			IOStreams ioStreams = new IOStreams(socket);
			Command cmd = readCommand(ioStreams);
			
			switch (cmd.getNumCommand()) {
			
			case Constants.CMD_SERVER_SAVE_FILE:
				byte[] receivedFile = Base64.decode(((ServerSaveFileCommand) cmd).getFile());
				String gid = ((ServerSaveFileCommand) cmd).getGid();
				writeReceivedFile(gid , receivedFile);
				
				if (RsDAO.getInstance().isFileExists(gid))
					RsDAO.getInstance().deleteByGID(gid);
				
				insertReceivedMetaData(cmd);
 
				//Send status
				ioStreams.getOutputStream().writeInt(Constants.OK);
				break;
				
			case Constants.CMD_SEND_User_INFO:
				MyInfo receivedUser = ((SendUserInfoCommand) cmd).getMyInfo();
			 	int receivedUserID = receivedUser.getMyGid();
				List<EncryptedUser> users = ((SendUserInfoCommand) cmd).getEncryptedUsers();
								
				if (!RsDAO.getInstance().isUserExists(receivedUserID))
					RsDAO.getInstance().insertUserInfo(receivedUser);
			 	
				
				for (EncryptedUser user : users){
					if (!RsDAO.getInstance().isUserContactsExists(receivedUserID, user.getEncryptedGID()))
						RsDAO.getInstance().insertUserContacts(receivedUserID, user);
				}
			
				ioStreams.getOutputStream().writeInt(Constants.OK_USER);
		 break;
		 
			case Constants.CMD_GET_FILE_TO_SHARE:
			 	sendFileToShare(((GetFileToShareCommand) cmd).getFileGID(),ioStreams);
			 
			 	break;
			case Constants.CMD_RECOVER_FILES:
			 	int userID_F = ((RecoverFilesCommand) cmd).getUserID();
				ArrayList<String> userFiles_F = RsDAO.getInstance().getGidByUserID(userID_F);				
				
				ArrayList<FileStored> files = new ArrayList<FileStored>() ;
				for (int i = 0 ; i < userFiles_F.size() ; i++)
					files.add(getFileFromRS(userFiles_F.get(i)));
				
				//Send information to Client
				List<EncryptedUser> usersE = RsDAO.getInstance().getContactsByUserID(userID_F);
				MyInfo user = RsDAO.getInstance().getUserByID(userID_F);
				
				Info info = new Info(files, usersE, user);
				//Send all the data to the client 
				ioStreams.getOutputStream().writeObject(info);
				
			 break;
				
			default:
				System.out.println("unknown command");
				break;
			}
			
			ioStreams.close();
			socket.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	   * Writes the received file.
	   * @param 
	   * 		filePath the file's path
	   * @param 	
	   * 		file the file itself
	   */
	public void writeReceivedFile(String gid, byte[] file) {
		FileOutputStream fos;
		try {
			// Creation of the received folder
			String tcellpath = Constants.RS_Files_PATH;
			if (!Tools.createDir(tcellpath))
				return;

			/* Write the file in the received folder */
			fos = new FileOutputStream(tcellpath + gid);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(file);
			
			bos.close();
			fos.close();

		} catch (FileNotFoundException ex) {
			System.err.println("ERROR : " + gid + " not found");
			return;
		} catch (IOException ex) {
			System.err.println("ERROR : can not write the file " + gid);
			return;
		}
	}

	private void insertReceivedMetaData(Command cmd) {
		try {
			int userID = ((ServerSaveFileCommand) cmd).getUserID();
			String gid = ((ServerSaveFileCommand) cmd).getGid();
			String command = ((ServerSaveFileCommand) cmd).getCommand();
			
			String encrIv = ((ServerSaveFileCommand) cmd).getIv();
			String StrEncryptedSkey =  ((ServerSaveFileCommand) cmd).getSkey();

			//Requete insert into RS database
			RsDAO.getInstance().insertFile(userID, gid, StrEncryptedSkey, encrIv, command);
		}
		catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	private FileStored getFileFromRS(String fileGID) {
		FileStored fileToRecover = null;
		try {
			FileDesc fileDesc = RsDAO.getInstance().getFileDescByGid(fileGID);
			Path path = Paths.get(fileDesc.fileID);
			byte[] file = Files.readAllBytes(path);
			
			fileToRecover = new FileStored(fileDesc, Base64.encode(file)); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileToRecover;
	}

	/**
	    * Sends a file to a client through a stream.
	    * @param ioStreams
	    * 			    an IOStreams object
	    * @param fileGID 
	    * 				the FullFileName of the requested File
	    * @throws IOException 
	    */
	private void sendFileToShare(String fileGID, IOStreams ioStreams) {
		try {
			FileDesc fileDesc = RsDAO.getInstance().getFileDescByGid(fileGID);
			Path path = Paths.get(fileDesc.fileID);
			byte[] file = Files.readAllBytes(path);
			FileStored fileToShare = new FileStored(fileDesc, Base64.encode(file)); 
			
			ioStreams.getOutputStream().writeObject(fileToShare);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public Command readCommand(IOStreams stream) {
		 /**
		   * readCommand Receives Command.
		   * @param ioStreams
		   * 				 an IOStreams object
		   */
		Command cmd = null;
		try {
			cmd = (Command) stream.getInputStream().readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cmd;
	}
}
