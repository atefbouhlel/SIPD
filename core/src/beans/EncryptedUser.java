package beans;

import java.io.Serializable;

public class EncryptedUser implements Serializable {

	private String encryptedGID;
	private String encryptedTCellIP;
	private String encryptedPort;
	private String encryptedPKey;
	
	public String getEncryptedGID() {
		return encryptedGID;
	}

	public void setEncryptedGID(String encryptedGID) {
		this.encryptedGID = encryptedGID;
	}

	public String getEncryptedTCellIP() {
		return encryptedTCellIP;
	}

	public void setEncryptedTCellIP(String encryptedTCellIP) {
		this.encryptedTCellIP = encryptedTCellIP;
	}

	public String getEncryptedPort() {
		return encryptedPort;
	}

	public void setEncryptedPort(String encryptedPort) {
		this.encryptedPort = encryptedPort;
	}

	public String getEncryptedPKey() {
		return encryptedPKey;
	}

	public void setEncryptedPKey(String encryptedPKey) {
		this.encryptedPKey = encryptedPKey;
	}

	public EncryptedUser(String encryptedGID, String encryptedTCellIP, String encryptedPort, String encryptedPKey) {
		super();
		this.encryptedGID = encryptedGID;
		this.encryptedTCellIP = encryptedTCellIP;
		this.encryptedPort = encryptedPort;
		this.encryptedPKey = encryptedPKey;
	}
}