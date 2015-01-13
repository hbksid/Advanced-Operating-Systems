//package Koo_Toueg_Protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinalTest {

	public static int noOfConsistentGlobalStates = 0;
	public static int noOfInconsistentGlobalStates = 0;

	private static Integer noOfNodes = 0;
	private static boolean foundAllLines = true;

	// For the consistecy check
	private static ArrayList<Integer[]> vClocks = new ArrayList<>();
	private static int[] vMAX;

	//public static void main(String[] args) throws IOException {

		public static void Checking() throws IOException{

		System.getProperties();
		// int noOfConsistentGlobalStates = 0;
		// int noOfInconsistentGlobalStates = 0;
		// Pattern checkpointingFilePattern =
		// Pattern.compile("^\\s*([0-9]+)\\s*_\\s*checkpointFile");//1_checkpointFile
		Pattern checkpointingFilePattern = Pattern
				.compile("^lastClock(.*).txt");// ("^\\s*([0-9]+)\\s*_\\s*"+str+"File");
		HashMap<Integer, BufferedReader> cpFiles = new HashMap<>();
		for (File file : ((new File("/home/005/r/rt/rtr100020/CS_6378/Project3/"))
				.listFiles())) {
			Matcher checkpointingFileMatcher = checkpointingFilePattern
					.matcher(file.getName().toString().trim());
			if (checkpointingFileMatcher.find()) {
				cpFiles.put(Integer.parseInt(checkpointingFileMatcher.group(1)
						.trim()), (new BufferedReader(new FileReader(
						"/home/005/r/rt/rtr100020/CS_6378/Project3/"
								+ file.getName().toString().trim()))));
			}
		}
		FinalTest.noOfNodes = cpFiles.size();
		FinalTest.vMAX=new int[FinalTest.noOfNodes];
		HashMap<Integer, String> current_NodeLines = new HashMap<>();
		ArrayList<Integer> nodes = new ArrayList<>(cpFiles.keySet());
		Collections.sort(nodes);
		int count=0;
		while (FinalTest.foundAllLines) {
			for (Integer node : nodes) {
				String line;
				if ((line = cpFiles.get(node).readLine()) != null) {
					current_NodeLines.put(node, line);
				} else {
					FinalTest.foundAllLines = false;
					break;
				}
			}
			ArrayList<Integer[]> vectorClocks = new ArrayList<>();
			if (FinalTest.foundAllLines) {
				for (Integer node : nodes) {
					System.out.println(" node : " + node + " line : "
							+ current_NodeLines.get(node) + " size:"
							+ current_NodeLines.size());
					vectorClocks.add(FinalTest.getVClock(current_NodeLines
							.get(node)));
				}

				FinalTest.vClocks = vectorClocks;
				FinalTest.CalculateVMAX(count);
				count++;
				System.out.println("^^^^^^^^ " + FinalTest.vClocks.size());

				if (FinalTest.ConsistancyCheck()) {
					noOfConsistentGlobalStates++;
					System.out.println("Consistent Global State ");
				} else {
					noOfInconsistentGlobalStates++;
					System.out.println("ERROR : Inconsistent Global State ");
				}
			}

			System.out
					.println("*********************  END OF ONE GLOBAL STATE CONSISTENCY CHECK  *******************************");
		}
		System.out
				.println("####################################################################################################");
		System.out
				.println("##################   NO OF CONSISTENT   GLOBAL STATES = "
						+ noOfConsistentGlobalStates
						+ "  #########################################");
		System.out
				.println("##################   NO OF INCONSISTENT GLOBAL STATES = "
						+ noOfInconsistentGlobalStates
						+ "  #########################################");
		// close on all the files

	}

	private static Integer[] getVClock(String line) {
		Integer[] vClock = new Integer[FinalTest.noOfNodes];
		String substrring[] = line.split("\\s+");
		if (substrring != null) {
			for (int i = 0; i < substrring.length; i++) {
				vClock[i] = Integer.parseInt(substrring[i].trim());
				System.out.println(" --- " + substrring[i].trim());
			}

		} else {
			System.out
					.println("ERROR : checkpointing line doesn't have vector clock entry");
		}
		return vClock;
	}

	private static boolean ConsistancyCheck() {
		boolean consistant = true;
		System.out.print("[ ");
		for (int i = 0; i < FinalTest.noOfNodes; i++) {
			System.out.println("&&&&&  for NODE NO : " + i);
			for (int j = 0; j < FinalTest.noOfNodes; j++) {
				if (j != i) {
					System.out.println("    ******  J : " + j);
					for (int k = j + 1; k < FinalTest.noOfNodes; k++) {
						if (k != i) {
							System.out.println("        $$$$$ P(" + j + ")["
									+ j + "] > P(" + k + ")[" + j + "]");
							System.out.println("        $$$$$ P(" + k + ")["
									+ k + "] > P(" + j + ")[" + k + "]\n");
							if ((FinalTest.vClocks.get(j)[j] < FinalTest.vClocks
									.get(k)[j])
									&& (FinalTest.vClocks.get(k)[k] > FinalTest.vClocks
											.get(j)[k])) {
								consistant = false;

							}
						}
						if (consistant == false)
							return false;
					}
				}
			}
		}
		System.out.println("]");
		return consistant;
	}

	private static Integer[] getVClock1(String line) {
		Integer[] vClock = new Integer[FinalTest.noOfNodes];
		String substrring[] = line.split("<");
		String vectorValues[];
		if (substrring != null) {
			vectorValues = substrring[1].split(" ");
			int i = 1;
			while ((!vectorValues[i].trim().equals(">")) && i <= vClock.length
					&& i <= vectorValues.length) {
				System.out.println("----" + vectorValues[i].trim() + "---");
				vClock[i - 1] = Integer.valueOf(vectorValues[i].trim());
				i++;
			}
		} else {
			System.out
					.println("ERROR : checkpointing line doesn't have vector clock entry");
		}
		return vClock;
	}

	public static void CalculateVMAX(int iteration) {
		int Max=FinalTest.vClocks.get(iteration)[iteration];
	}

	public static boolean ConsistancyCheck1() {
		boolean consistant = true;
		System.out.print("[ ");
		for (int i = 0; i < FinalTest.noOfNodes; i++) {
			System.out.print("p" + (i + 1) + " ");
		}
		System.out.print("] [ ");
		String s = "";
		for (int j = 0; j < FinalTest.noOfNodes; j++) {
			if (FinalTest.vMAX[j] != FinalTest.vClocks.get(j)[j]) {
				s = s + "X ";
				consistant = false;
			} else {
				s = s + "T ";
			}
		}
		System.out.println(s + "]");
		return consistant;
	}
}