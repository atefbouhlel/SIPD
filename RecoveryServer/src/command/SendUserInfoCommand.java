package command;

import beans.MyInfo;

import java.util.List;

import beans.EncryptedUser;

public class SendUserInfoCommand extends Command {
	private MyInfo myInfo;
	private List<EncryptedUser> EncryptedUsers;
	
	public SendUserInfoCommand(int numCommand, MyInfo myInfo, List<EncryptedUser> EncryptedUsers) {
		super(numCommand);
		this.myInfo = myInfo;
		this.EncryptedUsers = EncryptedUsers;
	}

	
	public MyInfo getMyInfo() {
		return myInfo;
	}


	public void setMyInfo(MyInfo myInfo) {
		this.myInfo = myInfo;
	}


	public List<EncryptedUser> getEncryptedUsers() {
		return EncryptedUsers;
	}


	public void setEncryptedUsers(List<EncryptedUser> EncryptedUsers) {
		this.EncryptedUsers = EncryptedUsers;
	}
}
