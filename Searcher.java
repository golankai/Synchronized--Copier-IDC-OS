import java.io.File;

/**
 * Kai Golan Hashiloni
 * 208705277
 */

/**
 * A searcher thread.
 * Searches for files that end with a specific extension in all directories listed in a directory queue.
 */
public class Searcher implements Runnable {
	
	private int id;
	java.lang.String extension;
	private SynchronizedQueue<File> directoryQueue;
	private SynchronizedQueue<String> milestonesQueue;
	private SynchronizedQueue<File> resultsQueue;
	private boolean isMilestones;
	
	/**
	 * Constructor. 
	 * @param int id the id of the thread
	 * @param java.lang.String extension to be searched
	 * @param SynchronizedQueue<File> directoryQueue synchronized queue for the sub directories
	 * @param SynchronizedQueue<String> milestonesQueue synchronized queue for the log of the class operations
	 * @param boolean isMilestones defines if write log or not
	 * @param SynchronizedQueue<File> resultsQueue synchronized queue for the results files
	 */
	public Searcher(int id, java.lang.String extension, SynchronizedQueue<File> directoryQueue,
			SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
		this.id = id;
		this.extension = extension;
		this.directoryQueue = directoryQueue;
		this.milestonesQueue = milestonesQueue;
		this.isMilestones = isMilestones;
		this.resultsQueue = resultsQueue;
	}
	
	/**
	 * Runs the searcher thread.
	 * Fetch a directory to search in from the directory queue.
	 * Search all files inside it (but will not recursively search sub directories!).
	 * Files that have the wanted extension are enqueued to the results queue.
	 * If specified, writes log to the milestonesQueue
	 */
	public void run() {
		File root;
		File[] files;
		
		if (isMilestones) {
			milestonesQueue.registerProducer();
		}
			
		resultsQueue.registerProducer();
		root = directoryQueue.dequeue();

		while (root != null) {	
			if (directoryQueue.getCount() >= DiskSearcher.MAX_FILES) {
				break;
			}
			files = root.listFiles(File::isFile);
			for (File f : files) {
				if (f.getName().endsWith(extension)) {
					if (isMilestones) {
						milestonesQueue.enqueue("Searcher on thread id " + id + ": file named " + f + " was found");
					}
					resultsQueue.enqueue(f);
				}
			}
			root = directoryQueue.dequeue();
		}
		
		resultsQueue.unregisterProducer();
		if (isMilestones) {
			milestonesQueue.unregisterProducer();
		}
	}
}
