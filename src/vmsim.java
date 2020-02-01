
/**
 * vmsim
 * CSC 452 Project 4 Virtual Memory Simulator
 * @author DennyHo
 * 
 * Program simulates the optimal, FIFO, random, and clock algorithms
 * for page replacements done in the operating system. Program assumes user 
 * will provide number of frames, the algorithm(opt|clock|fifo|rand), 
 * and a trace file in the command line arguments.
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class vmsim {
	// Fields that will be updated and printed
	private static int accesses = 0;
	private static int faults = 0;
	private static int writes = 0;
	private static int pageTableSize = 0;
	private static Scanner sc = null;
	private static Scanner sc2 = null;
	// Page Table and RAM
	private static PTE[] RAM;
	private static PTE[] pageTable;
	private final static int PAGESIZE = (int) Math.pow(2, 13); // Pages are 8KB
	// Command Line Arguments
	private static String algorithm = null;
	private static int numFrames = 0;
	// Common fields among the algorithms
	private static Random rand = new Random();
	private static String action = null;
	private static int counter = 0;
	private static Map<Integer, ArrayList<Integer>> map;

	/**
	 * Main reads the arguments, the file, and runs the algorithms.
	 * 
	 * @param args Command Line Arguments
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String filename = null;
		if (args[0].equals("-n") && args[2].equals("-a")) {
			// Absolute file path for eclipse
			filename = System.getProperty("user.dir") + "/src/" + args[4];
			// Filename for terminal compilation
			//filename = args[4];
			algorithm = args[3];
			numFrames = Integer.parseInt(args[1]);
		}

		File file = new File(filename);
		//File file = new File("/Users/DennyHo/eclipse-workspace/csc452-Project 4/src/ls.trace");

		try {
			sc = new Scanner(file);
			sc2 = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		RAM = new PTE[numFrames];
		pageTable = createPageTable();

		pageTableSize = (int) (Math.pow(2, 32) / PAGESIZE * 4);
		if (algorithm.equals("opt")) {
			createOptMap();
		}
		runAlgorithm();

		System.out.println();
		System.out.println("Algorithm: " + algorithm);
		System.out.println("Number of frames: " + numFrames);
		System.out.println("Total memory accesses: " + accesses);
		System.out.println("Total page faults: " + faults);
		System.out.println("Total writes to disk: " + writes);
		System.out.println("Total size of page table: " + pageTableSize + " bytes");
	}

	/**
	 * Runs the optimal algorithm to decide which page should be replaced
	 * 
	 * Replaced page is determined with the help of the map. Method calculates the
	 * distance from this current instruction to the next usage for each page in
	 * RAM. The farthest page that will need to be used will be replaced.
	 * 
	 * @return the frame number that will be replaced
	 */
	public static int runOpt() {
		int replacedPage = 0;
		// Check every frame's page to see when the further
		int minDistance = -1;
		for (int i = 0; i < RAM.length; i++) {
			ArrayList<Integer> list = map.get(RAM[i].getPageNumber());
			// No more future instructions
			if (list.size() <= 0) {
				replacedPage = i;
				break;
			}
			int j;
			// Check all the entries in the instructions index array 
			for (j = 0; j < list.size(); j++) {
				int distance = list.get(j) - accesses;
				if (distance <= 0) {
					continue;
				} else {
					// Get largest distance
					if (distance > minDistance) {
						minDistance = distance;
						replacedPage = i;
					}
					break;
				}
			}
			// Remove the instructions that have
			// already been run from the list
			while (list.size() > 0 && list.get(0) - accesses <= 0) {
				list.remove(0);
			}
		}
		return replacedPage;
	}

	/**
	 * Creates a map of pages to the run order of all of its instructions.
	 * 
	 * If a page's instructions are ran first then third then it will be mapped to
	 * an array list of 0 and 2.
	 */
	public static void createOptMap() {
		String line = null;
		map = new HashMap<Integer, ArrayList<Integer>>();
		int lineCounter = 0;
		// Parse through the file to get the pageNumbers
		while (sc2.hasNextLine()) {
			line = sc2.nextLine();
			if (line.charAt(0) != ' ' && line.charAt(0) != 'I')
				continue;
			String[] strs = line.substring(2).replaceAll("\\s", "").split(",");
			String addressStr = "0x" + strs[0].toUpperCase();
			long address = Long.decode(addressStr);
			int pageNum = (int) (address / PAGESIZE);

			// Map the Page to the Array
			if (map.isEmpty()) {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(lineCounter);
				map.put(pageNum, list);
			} else {
				// Map is not empty and contains key
				if (map.containsKey(pageNum)) {
					map.get(pageNum).add(lineCounter);
				} else {
					// New entry
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(lineCounter);
					map.put(pageNum, list);
				}
			}
			lineCounter++;
		}
	}

	/**
	 * Runs the similar parts of each of the page replacement algorithms.
	 * 
	 * All four page replacement algorithms must first check if there are empty
	 * frames to fill. This method eliminates repetitive code in each algorithm.
	 * When the algorithm needs to determine which page to evict, the unique
	 * algorithms come into play.
	 * 
	 * @throws IOException
	 */
	public static void runAlgorithm() throws IOException {
		int numFullFrames = 0;

		String line = null;
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (line.charAt(0) != ' ' && line.charAt(0) != 'I')
				continue;
			// Parse Line into Instruction, Address, and Size
			String instr = line.substring(0, 2).replaceAll("\\s", "");
			String[] strs = line.substring(2).replaceAll("\\s", "").split(",");
			String addressStr = "0x" + strs[0].toUpperCase();
			long address = Long.decode(addressStr);
			int size = Integer.parseInt(strs[1]);

			int pageNum = (int) (address / PAGESIZE);
			int numPages = (int) (Math.pow(2, 32) / PAGESIZE);
			if (pageNum > numPages) {
				action = "page fault";
				faults++;
			}
			// There are empty Frames to fill
			if (numFullFrames < numFrames) {
				// Page is not in RAM, add Page to RAM
				if (pageTable[pageNum].getFrameNumber() == -1) {
					RAM[numFullFrames] = pageTable[pageNum];
					if (instr.equals("S") || instr.equals("M"))
						RAM[numFullFrames].setDirty(true);
					RAM[numFullFrames].setFrameNumber(numFullFrames);
					RAM[numFullFrames].setRef(true);
					RAM[numFullFrames].setValid(true);
					pageTable[pageNum] = RAM[numFullFrames];
					action = "page fault - no eviction";
					numFullFrames++;
					faults++;
				} else {
					// Page is already in RAM
					int frameNum = pageTable[pageNum].getFrameNumber();
					RAM[frameNum].setRef(true);
					if (instr.equals("S") || instr.equals("M"))
						RAM[frameNum].setDirty(true);
					action = "hit";
				}
			} else if (numFullFrames == numFrames) {
				// There are no empty frames
				int replacedPage = 0;
				// There are no empty frames
				// Page is not in RAM, thus need to replace frame
				if (pageTable[pageNum].getFrameNumber() == -1) {
					if (algorithm.equals("rand")) // Random
						replacedPage = Math.abs(rand.nextInt()) % numFrames;
					else if (algorithm.equals("opt")) { // Optimal
						replacedPage = runOpt();
					} else if (algorithm.equals("fifo")) { // FIFO
						replacedPage = counter % numFrames;
					} else if (algorithm.equals("clock")) { // CLOCK
						while (RAM[counter % numFrames].isRef()) {
							RAM[counter % numFrames].setRef(false);
							counter++;
						}
						replacedPage = counter % numFrames;
					}
					// Page is dirty and thus must write data into disk
					if (RAM[replacedPage].isDirty()) {
						writes++;
						faults++;
						action = "page fault - dirty";
					} else {
						// Page is clean
						faults++;
						action = "page fault - clean";
					}
					// Replace page with new page
					RAM[replacedPage].setFrameNumber(-1);
					RAM[replacedPage].setDirty(false);
					RAM[replacedPage].setRef(false);
					RAM[replacedPage].setValid(false);

					pageTable[RAM[replacedPage].getPageNumber()] = RAM[replacedPage];
					RAM[replacedPage] = pageTable[pageNum];
					if (instr.equals("S") || instr.equals("M"))
						RAM[replacedPage].setDirty(true);
					RAM[replacedPage].setRef(true);
					RAM[replacedPage].setValid(true);
					RAM[replacedPage].setFrameNumber(replacedPage);
					counter++;
				} else {
					// Page is already in RAM
					int frameNum = pageTable[pageNum].getFrameNumber();
					RAM[frameNum].setRef(true);
					if (instr.equals("S") || instr.equals("M"))
						RAM[frameNum].setDirty(true);
					action = "hit";
				}
			}
			accesses++;
			System.out.println(addressStr + " " + action);
		}
	}

	/**
	 * Creates a Page Table
	 * 
	 * Size of the Page Table depends on the size of the Address Space and the
	 * Pages.
	 * 
	 * @return Page Table
	 */
	public static PTE[] createPageTable() {
		int numPages = (int) (Math.pow(2, 32) / PAGESIZE);
		PTE[] pt = new PTE[numPages];
		// Create empty pages
		for (int i = 0; i < numPages; i++) {
			PTE page = new PTE();
			pt[i] = page;
			page.setPageNumber(i);
		}
		return pt;
	}

}