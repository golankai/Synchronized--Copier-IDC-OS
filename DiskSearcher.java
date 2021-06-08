/**
 * Kai Golan Hashiloni
 * 208705277
 */

import java.io.File;

/**
 * Main application class.
 * This application searches for all files under some given path with the required extension
 * (that is given as an argument to the program).
 *  All files found are copied to some specific directory.
 */
public class DiskSearcher {
	final static int	DIRECTORY_QUEUE_CAPACITY = 50;
	final static int	RESULTS_QUEUE_CAPACITY = 50;
	final static int	MAX_DIRECTORIES = 50;
	final static int	MAX_FILES = 50;
	
	public DiskSearcher() {
	}
	
	public static void main(String[] args) {
		
		// Initialization and parsing arguments
		long startTime = System.nanoTime(), endTime, time;
		boolean isMilestones = Boolean.parseBoolean(args[0]);
		java.lang.String extension = args[1];
		
		String rootPath = args[2];
		File root = new File(rootPath);
		String destPath = args[3];
		File destination = new File(destPath);
		
		int numSearchers = Integer.parseInt(args[4]);
		Thread[] searchers = new Thread[numSearchers];
		int numCopiers = Integer.parseInt(args[5]);
		Thread[] copiers = new Thread[numCopiers];
		
		SynchronizedQueue<String> milestonesQueue = null;
		SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<File>(DIRECTORY_QUEUE_CAPACITY);
		SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<File>(RESULTS_QUEUE_CAPACITY);
		int i;
		
		
		if (isMilestones) {
			milestonesQueue = new SynchronizedQueue<String>(MAX_DIRECTORIES * MAX_FILES);
			milestonesQueue.registerProducer();
			milestonesQueue.enqueue("General, program has started the search");
		} 
		
		// working part
		Thread scouter = new Thread(new Scouter((int) (Thread.currentThread().getId()), directoryQueue,
					root, milestonesQueue, isMilestones));
		scouter.start();
		
		for (i = 0; i < numSearchers; i++) {
			searchers[i] = new Thread(new Searcher(i+10, extension, directoryQueue, resultsQueue, milestonesQueue, isMilestones));
			searchers[i].start();
		}
		
		for (i = 0; i < numCopiers; i++) {
			copiers[i] = new Thread(new Copier(i+20, destination, resultsQueue, milestonesQueue, isMilestones));
			copiers[i].start();
		}
		
		// Wait for all threads
		try {
			scouter.join();
		} catch (InterruptedException e) {
			System.out.println(e);
		}
		for (i = 0; i < numSearchers; i++) {
			try {
				searchers[i].join();
			} catch (InterruptedException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		for (i = 0; i < numCopiers; i++) {
			try {
				copiers[i].join();
			} catch (InterruptedException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		
		if (isMilestones) {
			milestonesQueue.printQueue();
		}
		endTime = System.nanoTime();
		time = endTime - startTime;
		System.out.println("Execution time in milliseconds: " + time / 1000000);
	}
}
