package messages;
import java.net.Socket;
import java.security.PrivateKey;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import beans.FileDesc;
import beans.FileStored;
import beans.Info;
import beans.User;
import command.Command;
import command.RecoverFilesCommand;
import cryptoTools.AsymmetricDecryption;
import cryptoTools.KeyGenerator;
import cryptoTools.KeyManager;
import tools.Constants;
import tools.IOStreams;

public class RecoverFiles {
	public static void Recover(String PKPath, User user){
		try {
			
			System.out.println("Recovering In Progress...");
			
			Socket socketServer = new Socket("127.0.0.1", 6666);
			IOStreams streamServer = new IOStreams(socketServer);
			
			int userID = user.getUserGID();
			
			// send the Recover command to the RS
			Command recoverCommand = new RecoverFilesCommand(Constants.CMD_RECOVER_FILES, userID);
			streamServer.getOutputStream().writeObject(recoverCommand);
			
			Info data = (Info) streamServer.getInputStream().readObject();			
			saveInfoTCell(data, PKPath, user);
			
			System.out.println("Recovery ended succesfully.");

			streamServer.close();
			socketServer.close();
						
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("RS not connected!");
		}
	}

	private static void saveInfoTCell (Info data, String PKPath,User user){		
		try{
		 	
		 	KeyGenerator keyGenarator = new KeyGenerator();
			PrivateKey privKey1 = keyGenarator.LoadPrivateKey(PKPath, Constants.RSA_ALG);
			
			KeyManager keyManager = new KeyManager();
			String privKeyString = keyManager.PrivateKeyToString(privKey1);
			
			PrivateKey privateKey = keyManager.StringToPrivateKey(privKeyString, Constants.RSA_ALG);
			
			for (int i = 0 ; i < data.getFiles().size() ; i++){
				String encryptedSkey = data.getFiles().get(i).getFileDesc().sKey; 
				String encryptedIv = data.getFiles().get(i).getFileDesc().iv;
			
				byte[] decSKey = AsymmetricDecryption.decryptBlockByBlock(Base64.decode(encryptedSkey),privateKey);
				byte[] decIv = AsymmetricDecryption.decryptBlockByBlock(Base64.decode(encryptedIv),privateKey);
				
				String DecryptSkey= Base64.encode(decSKey);
				String DecryptIv= Base64.encode(decIv);
				
				FileDesc fc = new FileDesc(data.getFiles().get(i).getFileDesc().fileID, DecryptSkey, DecryptIv, data.getFiles().get(i).getFileDesc().descr);
				FileStored fs = new FileStored(fc, data.getFiles().get(i).getFile());
				//Send the file to the TCell
				SendFileTcell.send(user,fs);
			}
		
		}
		catch(Exception e){
			System.out.println(e+"\nException ....");
		}
		
		//Send the personal in the tcell
		SendUserInfo.sendUserInfo(data.getUser(), data.getContacts());
	}

}
