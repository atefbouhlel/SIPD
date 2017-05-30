package command;

import beans.FileStored;

public class SendFileTcellCommand extends Command {
	private FileStored file;
	
	public SendFileTcellCommand(int numCommand, FileStored file) {
		super(numCommand);
		this.file = file;
	}

	public FileStored getFileToSend() {
		return file;
	}

	public void setFileToSend(FileStored file) {
		this.file = file;
	}

}
