package command;

public class RecoverFilesCommand extends Command{
	private int userID;
	
	public RecoverFilesCommand(int numCommand, int userID) {
		super(numCommand);
		this.userID = userID;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}
}
