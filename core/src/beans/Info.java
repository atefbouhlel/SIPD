package beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Info implements Serializable {

	private ArrayList<FileStored> files;
	private List<EncryptedUser> contacts;
	private MyInfo user;
	
	public ArrayList<FileStored> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<FileStored> files) {
		this.files = files;
	}
	
	public List<EncryptedUser> getContacts() {
		return contacts;
	}

	public void setContacts(List<EncryptedUser> contacts) {
		this.contacts = contacts;
	}

	public MyInfo getUser() {
		return user;
	}

	public void setUser(MyInfo user) {
		this.user = user;
	}

	public Info(ArrayList<FileStored> files, List<EncryptedUser> contacts, MyInfo user) {
		super();
		this.files = files;
		this.contacts = contacts;
		this.user = user;
	}

	
}
