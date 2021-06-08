import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Kai Golan Hashiloni
 * 208705277
 */

/**
 * lists all sub-directories from a given root path.
 */
public class Scouter implements Runnable {
	private int id;
	private SynchronizedQueue<File> directoryQueue;
	private File root;
	private SynchronizedQueue<String> milestonesQueue;
	private boolean isMilestones;


	/**
	 * Constructor. 
	 * @param int id the id of the thread
	 * @param SynchronizedQueue<File> directoryQueue synchronized queue for the sub directories
	 * @param File root the address to start searching from
	 * @param SynchronizedQueue<String> milestonesQueue synchronized queue for the log of the class operations
	 * @param boolean isMilestones defines if write log or not
	 */
	public Scouter (int id, SynchronizedQueue<File> directoryQueue,
			File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
		this.id = id;
		this.directoryQueue = directoryQueue;
		this.root = root;
		this.milestonesQueue = milestonesQueue;
		this.isMilestones = isMilestones;
	}
	
	/**
	 * Run the Scouter.
	 * Add all sub directories to the queue.
	 * If specified, writes log to the milestonesQueue
	 */
	public void run() {
		Queue<File> localDirsQ = new LinkedList<>();
		File dir;
		File[] subDirs;
		
		directoryQueue.registerProducer();
		
		localDirsQ.add(root);
		if (isMilestones) {
			milestonesQueue.registerProducer();
			milestonesQueue.enqueue("Scouter on thread id " + id + ": directory named " + root + " was scouted");
		}
		while (!localDirsQ.isEmpty()) {
			if (directoryQueue.getCount() >= DiskSearcher.MAX_DIRECTORIES) {
				break;
			}
			dir = localDirsQ.remove();
			
			// adds the current scouted dir to the directory queue
			directoryQueue.enqueue(dir);

			// Extract a sub directories list
			subDirs = dir.listFiles(File::isDirectory);
			
			// add all sub directories of the current directory to the local queue
			for (File f : subDirs) {
				localDirsQ.add(f);
				if (isMilestones) {
					milestonesQueue.enqueue("Scouter on thread id " + id + ": directory named " + f + " was scouted");
				}
			}
		}
		
		directoryQueue.unregisterProducer();
		if (isMilestones) {
			milestonesQueue.unregisterProducer();
		}
	}
}