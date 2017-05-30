package command;

public class ServerSaveFileCommand extends Command {

	private String file;
	private String gid;
	private String skey;
	private String iv;
	private int userID;
	private String command;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public ServerSaveFileCommand(int numCommand, String file, String gid, String skey, String iv, int userID,String command) {
		super(numCommand);
		this.file = file;
		this.gid = gid;
		this.skey = skey;
		this.iv = iv;
		this.userID = userID;
		this.command = command;
	}
}
