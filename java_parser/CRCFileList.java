/*
Copyright Giulio URLINI, 2014
*/

/* This program searches in a given directory duplicates of the same file. The method computes the CRC code of each file, and group any match. In case of a positive mathc a binary comarison is performed.
* 
* New Features:
* 
* TODO add the support of multiple dorectory search
* TODO add the option for limit the list to the single files, and not the matches
*
*/

import java.io.*;
import java.util.zip.*;
import java.util.*;

public class CRCFileList {

	public static String def_listfilename = "scanlist.txt";
	public static final int max_buffer_size = 250000;
	public RandomAccessFile fileList;
	private ArrayList<FileCRCItem> listOfMatches;
	private static boolean fullList = false;

	public static void usage () {
		System.out.println("Usage:  java  CRCFileList  -r root_dir [-r any_other_dir [-r other...]] [-f listfilename] [-a]");
		System.out.println("");
		System.out.println("        -r directory: add a directory to the list of locations to be scanned");
		System.out.println("        -f filename: specifices the file where the list of scanned files will be stored");
		System.out.println("        -a: if this option is specifcied all the files will be listed. If not");
		System.out.println("            the default is to list only multiple files that share the same crc");
		System.out.println("");
		System.exit(3);
	}
	
	public static void main(String args[]) {
		int index = 0;
		String listfilename = new String("");
		String root_directories[] = new String[0];
		String temp_dir[] = new String[0];
		int num_of_roots = 0;
		boolean found_def_root = false;
		if(args.length<1) {
			usage();
		}
		while (index < args.length ) {
			if (args[index].compareTo("-r") == 0) {
				if (args.length > index+1 ) {
					temp_dir = new String[root_directories.length + 1];
					for (int i = 0; i<root_directories.length; i++) {
						temp_dir[i] = new String(root_directories[i]);
					}
					temp_dir[root_directories.length] = new String(args[index+1]);
					root_directories = new String[temp_dir.length];
					for (int i = 0; i<temp_dir.length; i++) {
						root_directories[i] = new String(temp_dir[i]);
					}
					index +=2;
					found_def_root = true;
				} else usage();
			}
			if (args[index].compareTo("-f") == 0) {
				if (args.length > index+1 ) {
					listfilename = new String(args[index+1]);
					index += 2;
				} else usage();
			} else if (args[index].compareTo("-a") == 0) {
				fullList = true;
				index++;
			}
		}
		if (!found_def_root) {
			System.out.println("Please specifiy at least one root directory");
			System.out.println("");
			usage();
		}
		if ()
		CRCFileList zzz = new CRCFileList(root_directories, listfilename);
	}

	CRCFileList(String []rootdir, String listfilename) {
		try {
			listOfMatches = new ArrayList<FileCRCItem>();
			fileList = new RandomAccessFile(root_dir.getAbsolutePath() + File.separator + listfilename, "rw");
			if (fileList.length() > 0) {
			// the list file already exists. Reset it
				fileList.setLength(0);
			}
			for (int roots_index = 0; roots_index<rootdir.length; roots_index++) {
				File root_dir = new File(rootdir[roots_index]);
				if (!root_dir.exists()) {
					p("The root directory specificed (" + rootdir[roots_index] + ") does not exists");
					System.exit(3);
				}
				if (!root_dir.isDirectory()) {
					p("The root directory specificed (" + rootdir[roots_index] + ") is not a directory");
					System.exit(3);
				}

				processDirectory(root_dir);
			}
			for (int i = 0; i<listOfMatches.size(); i++) {
			p("Item " + i);
				FileCRCItem item = listOfMatches.get(i);
				if (fullList) {
					
					fileList.write((Long.toHexString(item.getCRC())).getBytes());
					fileList.write('\n');
					for (int j = 0; j< item.length(); j++) {
						fileList.write(item.getItem(j).getBytes());
						fileList.write('\n');
					}
				} else {
					if (item.length()>1) {
						fileList.write((Long.toHexString(item.getCRC())).getBytes());
						fileList.write('\n');
						for (int j = 0; j< item.length(); j++) {
							fileList.write(item.getItem(j).getBytes());
							fileList.write('\n');
						}
					}
				}
			}
			fileList.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void processDirectory(File directory) {
		try {
			File[] filesInCurrentDir = directory.listFiles();
			for (int i = 0; i<filesInCurrentDir.length; i++) {
				if (filesInCurrentDir[i].isDirectory()) {
					processDirectory(filesInCurrentDir[i]);
				} else {
					FileInputStream fileReader = new FileInputStream(filesInCurrentDir[i]);
					byte array[] = new byte[max_buffer_size];
					int data_read = 0;
					CRC32 crcProcessor = new CRC32();
					while (data_read >=0) {
						data_read = fileReader.read(array);
						if (data_read > 0) {
							crcProcessor.update(array, 0, data_read);
						}
					}
					if (listOfMatches.isEmpty()) {
						FileCRCItem item = new FileCRCItem(crcProcessor.getValue(),  filesInCurrentDir[i].getAbsolutePath());
						listOfMatches.add(item);
					} else {
						boolean found_match = false;
						for (int j = 0; j<listOfMatches.size(); j++) {
							FileCRCItem item = listOfMatches.get(j);
							if (item.compareCRC(crcProcessor.getValue())) {
								item.addItem(filesInCurrentDir[i].getAbsolutePath());
								found_match = true;
							}
						}
						if (!found_match) {
							listOfMatches.add(new FileCRCItem(crcProcessor.getValue(),  filesInCurrentDir[i].getAbsolutePath()));
						}
					}
					p("File " + filesInCurrentDir[i].getAbsolutePath() + " CRC " + crcProcessor.getValue());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected void p(String s) { System.out.println(s); }
	protected void pp(String s) { System.out.print(s); }


	protected String leftPad(int i, int length) {
		return leftPadInt(i, length, ' '); }

	protected String leftPad(double d, int length) {
		return leftPadDouble(d, length, 0, ' '); }

	//	leftPad("12", 5)          -->    "   12"
	//	leftPad("a8", 6, '0')     -->   "0000a8"
	//	leftPad("hello", 7, '-')  -->  "--hello"
	protected String leftPad(String s, int length) {
		return leftPadString(s, length); }

	protected String leftPadString(String s, int length) {
		return leftPadString(s, length, ' '); }

	protected String leftPadString(String s, int length, char padChar) {
		String temp = "";
		int i;
		for(i=0; i<length; i++)
			temp = temp + padChar; 
		temp = temp + s;
		i = temp.length();
		return(temp.substring(i-length));
	}
	
	protected String leftPadInt(int val, int length) {
		return(leftPadString(Integer.toString(val), length, ' ')); }

	protected String leftPadInt(int val, int length, char padChar) {
		return(leftPadString(Integer.toString(val), length, padChar)); }

	protected String leftPadIntHex(int val, int length, char padChar) {
		return(leftPadString(Integer.toHexString(val), length, padChar)); }


	private String leftPadDouble(double val, int integerDigits, int fractionalDigits) {
		return leftPadDouble(val, integerDigits, fractionalDigits, ' ', true); }

	private String leftPadDouble(double val, int integerDigits, int fractionalDigits, char leftPadChar) {
		return leftPadDouble(val, integerDigits, fractionalDigits, leftPadChar, true); }

	private String leftPadDouble(
			double val,
			int integerDigits,
			int fractionalDigits,
			char leftPadChar,
			boolean roundValueAtLastFractionDigit ) {

		boolean lessThanZero = false;
		int lessThanZeroInt = 0;

		if(val<0) {
			lessThanZero = true;
			val = - val;
			lessThanZeroInt = 1;
		}

		if(integerDigits>18)
			integerDigits = 18;

		if(fractionalDigits>18)
			fractionalDigits = 18;

		long fm = 1; // fraction multiplier

		for(int i=0; i<fractionalDigits; i++)
			fm = fm * 10;

		double rounding = 0.5;
		if(val<0)
			rounding = -rounding;

		if(!roundValueAtLastFractionDigit)
			rounding = 0;

		long pi = (long) (val + rounding / fm);   // part integer
		long pf = (long) ((val-((double)pi)) * fm + rounding); // part fractional       1.e+17

		String spi = "";

		if( (""+pi).length() + lessThanZeroInt > integerDigits )
			spi = "############################################".substring(0, integerDigits);
		else {
			if( lessThanZero && (leftPadChar == ' ') )
				spi = "                             ".substring(0, integerDigits - ((""+pi).toString().length()+lessThanZeroInt) ) + "-" + pi ;
			else
				spi = "" + ( lessThanZero ? "-" : "" ) + leftPadString(""+pi, integerDigits - lessThanZeroInt, leftPadChar);
		}

		if( fractionalDigits==0 )
			return("" + spi);

		return("" + spi + "." + leftPadString(Long.toString(pf), fractionalDigits, '0'));

	} // leftPadDouble

	protected void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) { e.printStackTrace(); }
	}
  protected long getTime() { return System.currentTimeMillis(); }

} // class

