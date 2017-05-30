/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package daemon;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import javax.crypto.SecretKey;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import beans.FileDesc;
import beans.FileRead;
import beans.FileStored;
import beans.User;
import command.Command;
import command.GetFileToShareCommand;
import command.GetUserCommand;
import command.ReadFileCommand;
import command.SendFileTcellCommand;
import command.ShareFileCommand;
import command.StoreFileCommand;
import configuration.Configuration;
import cryptoTools.AsymmetricDecryption;
import cryptoTools.AsymmetricEncryption;
import cryptoTools.SymmetricDecryption;
import dao.TcellDAOToken;
import messages.ServerSaveFile;
import tools.Constants;
import tools.IOStreams;
import tools.SymKeyTools;
import tools.Tools;

/**
 * ThreadServer in TCell. It receives commands from clients and executes the
 * associated actions.
 *
 * @author Athanasia Katsouraki, Majdi Ben Fredj, TORKHANI Rami
 *
 */
public class ClientConnectionManager extends Thread {
	Socket socket;
	int userGID;

	public ClientConnectionManager(Socket s, int userGID) {
		/**
		 * Creates a ThreadServer instance
		 * @param socket the client socket
		 * @param userGID the userGID of the TC
		 */
		this.socket = s;
		this.userGID = userGID;
	}

	@Override
	public void run() {
		try {
			IOStreams ioStreams = new IOStreams(socket);
			Command cmd = readCommand(ioStreams);
			
			switch (cmd.getNumCommand()) {
			case Constants.CMD_STORE_FILE:
				try {
					writeReceivedFile(((StoreFileCommand) cmd).getFilePath(),Base64.decode(((StoreFileCommand) cmd).getFile()));
					
					insertReceivedMetaData(cmd);

					//Send status
					ioStreams.getOutputStream().writeInt(Constants.OK);
					TcellDAOToken.getInstance(false).printAllFiles();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case Constants.CMD_GET_FILES_DESC:
				sendFilesDesc(ioStreams);
				break;

			case Constants.CMD_READ_FILE:
				readAndSendFile(((ReadFileCommand) cmd).getFileGID(),ioStreams);
				break;
				
				
			 case Constants.CMD_GET_USER:
				 sendUser(((GetUserCommand) cmd).getUserGID(),ioStreams);
				 
			 break;
			
			 case Constants.CMD_GET_FILE_TO_SHARE:
				 sendFileToShare(((GetFileToShareCommand) cmd).getFileGID(),ioStreams);
				 
			 break;
			 
			 case Constants.CMD_SEND_FILE_TCELL:
				 byte[] receivedFile = Base64.decode(((SendFileTcellCommand) cmd).getFileToSend().getFile());					
				 String fileID = null;
				
					String temp = ((SendFileTcellCommand) cmd).getFileToSend().getFileDesc().fileID;
					
					String filePath = new File(temp).getParent();
					String fileGID = new File(temp).getName();				
					
					String[] tokens = fileGID.split("[|]");
					
					String fileName = tokens[1];
					
					fileID = filePath+"/"+fileName;
					
					System.out.println(fileID);

					writeReceivedFile(fileID, receivedFile);
					// 
					insertReceivedMetaData((((SendFileTcellCommand) cmd)).getFileToSend().getFileDesc());
	 
					//Send status
					ioStreams.getOutputStream().writeInt(Constants.OK);
					TcellDAOToken.getInstance(false).printAllFiles();
				 break;
				 
			 case Constants.CMD_SHARE_FILE:
				
				byte[] receivedFileS = Base64.decode(((ShareFileCommand) cmd).getFileToShare().getFile());
				writeReceivedFile(((ShareFileCommand) cmd).getFileToShare().getFileDesc().fileID, receivedFileS);
				
				insertReceivedMetaData((((ShareFileCommand) cmd)).getFileToShare().getFileDesc());
 
				//Send status
				ioStreams.getOutputStream().writeInt(Constants.OK);
				
					String file = ((ShareFileCommand) cmd).getFileToShare().getFile();
					String EncryptedFileName = new File(((ShareFileCommand) cmd).getFileToShare().getFileDesc().fileID).getName();
					User user = TcellDAOToken.getInstance(false).getUserById(userGID);
					
					String iv = ((ShareFileCommand) cmd).getFileToShare().getFileDesc().iv;
					String strDecryptSkeyS = ((ShareFileCommand) cmd).getFileToShare().getFileDesc().sKey;
					
					PublicKey pubKey = Tools.stringToPublicKey(user.getPubKey());
					byte[] encrSkeyS= AsymmetricEncryption.encryptBlockByBlock( Base64.decode(strDecryptSkeyS), pubKey);
					byte[] encrIvS= AsymmetricEncryption.encryptBlockByBlock( Base64.decode(iv), pubKey);
										
					ServerSaveFile.serverSaveFile(file, EncryptedFileName, Base64.encode(encrSkeyS), Base64.encode(encrIvS),user, "SHARE");
			
				TcellDAOToken.getInstance(false).printAllFiles();
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

	
	private void insertReceivedMetaData(FileDesc fileDesc) {
		/**
		 * insertReceivedMetaData inserting the file described in the database
		 * @param fileDesc
		 */
		TcellDAOToken.getInstance(false).insertFileDesc(fileDesc.fileGID, fileDesc.fileID, fileDesc.sKey, fileDesc.iv, "RECOVER", fileDesc.descr);
	}

	private void sendFileToShare(String fileGID, IOStreams ioStreams) {
		 /**
		    * Sends a file to a client through a stream.
		    * @param ioStreams
		    * 			    an IOStreams object
		    * @param fileGID 
		    * 				the FullFileName of the requested File
		    * @throws IOException 
		    */
		try {
			FileDesc fileDesc = TcellDAOToken.getInstance(false).getFileDescByGid(fileGID);
			Path path = Paths.get(fileDesc.fileID);
			byte[] file = Files.readAllBytes(path);
			FileStored fileToShare = new FileStored(fileDesc, Base64.encode(file)); 
			
			ioStreams.getOutputStream().writeObject(fileToShare);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendUser(int userGID, IOStreams ioStreams) {
		/**
		 * sendUser is able to send the requested User  
		 * @param userGID
		 * 			 	Requested user
		 * @param ioStreams
		 */
		try {
			User user = TcellDAOToken.getInstance(false).getUserById(userGID);
			ioStreams.getOutputStream().writeObject(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readAndSendFile(String fileGID, IOStreams ioStreams) {
		/** 
		 * readAndSendFile is able to read and send a File requested by Apps or TCs
		 * @param fileGID the FullFileName of the file requested
		 * @param ioStream
		 */
		try {
			String fgid = fileGID;
			FileDesc fileDesc = null;
			// Select SECRETKEY, FILEID from FILE WHERE FILEGID = 'fullFileName';
			fileDesc = TcellDAOToken.getInstance(false).getFileDescByGid(fgid);
			if (fileDesc != null) {
				/* Decryption of the file */
				String stringKey = fileDesc.sKey;
				String fileID = fileDesc.fileID;
				String iv = fileDesc.iv;

				SecretKey sKey = SymKeyTools.StringToGenSymKey(stringKey);

				byte[] iv_decoded = Base64.decode(iv.getBytes());
				byte[] file = decryptFile(fileID, sKey, iv_decoded);
				String filename = Tools.getFileName(fileID);
				String[] parts = filename.split(Constants.SYM_ENCR_FILE_NAME);
				filename = parts[1];
				String fullDecName = Configuration.getConfiguration().getProperty("tcellPath") + Constants.SYM_DECR_FILE_NAME + filename;
				FileRead decryFile = new FileRead(fullDecName, Base64.encode(file));

				/* Send the file */
				ioStreams.getOutputStream().writeObject(decryFile);
				System.out.println("File sent");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendFilesDesc(IOStreams ioStreams) {
		/**
	     * Sends files' description to a client through a stream.
	     * @param stream an IOStreams object
	     */

		try {
			List<FileDesc> listDesc = TcellDAOToken.getInstance(false).getAllFiles();

			if (listDesc == null) {
				System.err.println("None file description in the database");
				ioStreams.getOutputStream().writeInt(Constants.KO);
			} else {
				ioStreams.getOutputStream().writeInt(Constants.OK);
				/* Send the list */
				ioStreams.getOutputStream().writeObject(listDesc);
				System.out.println("Files descriptions sent");
			}
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
	
	private void insertReceivedMetaData(Command cmd) {

		try {
			String strPrivKey = TcellDAOToken.getInstance(false).getMyInfo().getMyPrivKey();
			PrivateKey privKey = Tools.stringToPrivateKey(strPrivKey);
			String filePath = ((StoreFileCommand) cmd).getFilePath();
			String type = "STORE";
			String iv = ((StoreFileCommand) cmd).getIv();
			String sKey = ((StoreFileCommand) cmd).getsKey();
			byte[] encrSKey = Base64.decode(sKey);
			byte[] decSKey = AsymmetricDecryption.decryptBlockByBlock(encrSKey,privKey);
			String strDecryptSkey= Base64.encode(decSKey);
			String fileGID = userGID + "|" + filePath;
			String fileID = Configuration.getConfiguration().getProperty("tcellPath")+ Tools.getFileName(fileGID);
			//chemin absolut
			//fileID = new File(fileID).getAbsolutePath();

			TcellDAOToken.getInstance(false).insertFileDesc(fileGID, fileID,strDecryptSkey, iv, type, "my file");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void writeReceivedFile(String filePath, byte[] file) {
		 /**
		   * Writes the received file.
		   * @param 
		   * 		filePath the file's path
		   * @param 	
		   * 		file the file itself
		   */
		FileOutputStream fos;
		try {
			/* Creation of the received folder */
			if (!Tools.createDir(Configuration.getConfiguration().getProperty("tcellPath")))
				return;

			/* Extract the file name from the path */
			String fileID = new File(filePath).getName();

			/* Write the file in the received folder */
			fos = new FileOutputStream(Configuration.getConfiguration().getProperty("tcellPath") + fileID);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(file);
			System.out.println("The file '" + Configuration.getConfiguration().getProperty("tcellPath") + fileID + "' (" + file.length + " bytes) has been received successfully!");
			bos.close();
			fos.close();

		} catch (FileNotFoundException ex) {
			System.err.println("ERROR : file " + filePath + " not found");
			return;
		} catch (IOException ex) {
			System.err.println("ERROR : can not write the file " + filePath);
			return;
		}
	}
	

	public static byte[] decryptFile(String fullEncFileName, SecretKey sKey, byte[] iv)
	{
		/**
		 * Decrypts the file.
		 * @param 
		 * 		fullEncFileName the path of the encrypted file
		 * @param 
		 * 		sKey the secret key
		 * @param
		 *	    iv the initialization vector
		 * @return the decrypted file
		 */
		byte[] decBytes = null;
		FileInputStream is;
		try 
		{
			//Decrypt the message

			String filename= Tools.getFileName(fullEncFileName);
			is = new FileInputStream(fullEncFileName);
			int dataSize= is.available();
			decBytes = new byte[dataSize];
			is.read(decBytes);

			String[] parts = filename.split(Constants.SYM_ENCR_FILE_NAME);
			filename = parts[1]; 
			String fullDecName = Configuration.getConfiguration().getProperty("tcellPath") + Constants.SYM_DECR_FILE_NAME + filename;
			FileOutputStream dos = new FileOutputStream(fullDecName );
			SymmetricDecryption.decryptFile(decBytes,sKey, dos, iv);

			decBytes = Tools.readFileFromPath ( fullDecName );
			if(decBytes == null)
				return null;
			
			Tools.deleteFileFromPath( fullDecName );
			
			dos.flush();
			is.close();
			dos.close();             


		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Can not decrypt the file " + fullEncFileName);
			return null;
		}

		return decBytes;
	}

	

}
