import java.util.*;
import java.io.*;

public class FileCRCItem {
	private ArrayList<String> fileList;
	private long CRCValue = 0;
	private static final int max_buffer_size=100000;

	FileCRCItem(long CRC, String firstFile) {
		fileList = new ArrayList<String>();
		fileList.add(firstFile);
		CRCValue = CRC;
	}

	boolean compareCRC(long extCRC) {
		if (extCRC == CRCValue) {
			return true;
		} else {
			return false;
		}
	}

	long getCRC() {
		return CRCValue;
	}

	boolean addItem(String filename) {
		try {
			File orig = new File(fileList.get(0));
			FileInputStream origStream = new FileInputStream(orig);
			File newfile = new File(filename);
			FileInputStream newStream = new FileInputStream(newfile);
			byte[] orig_buffer = new byte[max_buffer_size];
			byte[] new_buffer = new byte[max_buffer_size];
			int data_read_orig = 0;
			int data_read_new = 0;
			boolean different = false;
			while ((data_read_new >= 0) && (data_read_orig >= 0)) {
				data_read_orig = origStream.read(orig_buffer);
				data_read_new = newStream.read(new_buffer);
				for (int i = 0; i < max_buffer_size; i++) {
					if (orig_buffer[i] != new_buffer[i]) {
						different = true;
						break;
					}
				}
				if (different) {
					break;
				}
			}
			if (different) {
				return false;
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
		fileList.add(filename);
		return true;
	}

	String getItem(int index) {
		if (index > fileList.size()) {
			return null;
		}
		return fileList.get(index);
	}

	int length() {
		return fileList.size();
	}
}
