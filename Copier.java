/**
 * Kai Golan Hashiloni
 * 208705277
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A Copier thread.
 * Reads files to copy from a queue and copies them to the given destination.
 */
public class Copier implements Runnable {
	public static final int COPY_BUFFER_SIZE = 4096;
	private int id;
	private File destination;
	private SynchronizedQueue<File> resultsQueue;
	private SynchronizedQueue<String> milestonesQueue;
	private boolean isMilestones;
	
	/**
	 * Constructor. 
	 * @param int id the id of the thread
	 * @param File destination where to copy the files to
	 * @param SynchronizedQueue<File> resultsQueue synchronized queue for the results files
	 * @param SynchronizedQueue<String> milestonesQueue synchronized queue for the log of the class operations
	 * @param boolean isMilestones defines if write log or not
	 */
	public Copier(int id, File destination, SynchronizedQueue<File> resultsQueue,
			SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
		this.id = id;
		this.destination = destination;
		this.resultsQueue = resultsQueue;
		this.milestonesQueue = milestonesQueue;
		this.isMilestones = isMilestones;	
	}
	
	/**
	 * Runs the copier thread.
	 * Thread will fetch files from queue and copy them, one after each other, to the destination directory.
	 * When the queue has no more files, the thread finishes.
	 * If the isMilestones was set in the constructor, it should write every "important" action to this queue.
	 */
	public void run() {
		File copiedFile, curFile;
		if (isMilestones) {
			milestonesQueue.registerProducer();
		}
		
		curFile = resultsQueue.dequeue();
		while (curFile != null) {
			copiedFile = new File(destination, curFile.getName());
			try {
				Files.copy(curFile.toPath(), copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				if (isMilestones) {
					milestonesQueue.enqueue("Copier from thread id " + id + ": file named " + curFile + " was copied");
				}
			} catch (IOException e) {
				System.out.println(e);
			}
			curFile = resultsQueue.dequeue();
		}
		
		if (isMilestones) {
			milestonesQueue.unregisterProducer();
		}
	}
}
