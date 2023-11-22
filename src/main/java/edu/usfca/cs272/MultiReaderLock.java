package edu.usfca.cs272;

import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive. The
 * active writer is able to acquire read or write locks as long as it is active.
 *
 * <!-- simplified lock used for this class -->
 * @see SimpleLock
 *
 * <!-- built-in Java locks that are similar (but more complex) -->
 * @see Lock
 * @see ReentrantLock
 * @see ReadWriteLock
 * @see ReentrantReadWriteLock
 *
 * @author Honghuai(King) Ke
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class MultiReaderLock {
	/** The conditional lock used for reading. */
	private final SimpleLock readerLock;

	/** The conditional lock used for writing. */
	private final SimpleLock writerLock;

	/** The number of active readers. */
	private int readers;

	/** The number of active writers; */
	private int writers;

	/** The thread that holds the write lock. */
	private Thread activeWriter;

	/** The log4j2 logger. */
	private static final Logger log = LogManager.getLogger();

	/**
	 * The lock object used for synchronized access of readers and writers. For
	 * security reasons, a separate private final lock object is used.
	 *
	 * @see <a href=
	 *   "https://wiki.sei.cmu.edu/confluence/display/java/LCK00-J.+Use+private+final+lock+objects+to+synchronize+classes+that+may+interact+with+untrusted+code">
	 *   SEI CERT Oracle Coding Standard for Java</a>
	 */
	private final Object lock;

	/**
	 * Initializes a new simple read/write lock.
	 */
	public MultiReaderLock() {
		readerLock = new ReadLock();
		writerLock = new WriteLock();

		lock = new Object();

		readers = 0;
		writers = 0;

		activeWriter = null;
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock readLock() {
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock writeLock() {
		return writerLock;
	}

	/**
	 * Returns the number of active readers.
	 *
	 * @return the number of active readers
	 */
	public int readers() {
		synchronized (lock) {
			return readers;
		}
	}

	/**
	 * Returns the number of active writers.
	 *
	 * @return the number of active writers
	 */
	public int writers() {
		synchronized (lock) {
			return writers;
		}
	}

	/**
	 * Determines whether the thread running this code and the writer thread are in
	 * fact the same thread.
	 *
	 * @return true if the thread running this code and the writer thread are not
	 *   null and are the same thread
	 *
	 * @see Thread#currentThread()
	 */
	public boolean isActiveWriter() {
		synchronized (lock) {
			return Thread.currentThread().equals(activeWriter);
		}
	}

	/**
	 * A simple lock used for conditional synchronization as an alternative to using
	 * a {@code synchronized} block.
	 *
	 * Similar but simpler than {@link Lock}.
	 *
	 * @author CS 272 Software Development (University of San Francisco)
	 * @version Fall 2023
	 */
	public static interface SimpleLock {
		/**
		 * Acquires the lock. If the lock is not available then the current thread
		 * becomes disabled for thread scheduling purposes and lies dormant until the
		 * lock has been acquired.
		 */
		public void lock();

		/**
		 * Releases the lock.
		 */
		public void unlock();
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class ReadLock implements SimpleLock {
		/**
		 * Controls access to the read lock. The active thread is forced to wait while
		 * there are any active writers and it is not the active writer thread. Once
		 * safe, the thread is allowed to acquire a read lock by incrementing the number
		 * of active readers.
		 */
		@Override
		public void lock() {
			/*
			 * This starts with the basic implementation from lecture (plus some
			 * logging). You will eventually need to modify it to check for whether there is
			 * an active writer.
			 */
			try {
				// Note the lock object being used here and elsewhere
				synchronized (lock) {
					while (writers > 0 && !isActiveWriter()) {
						lock.wait();
					}

					readers++;
					log.debug("Locked Read");
				}
			} catch (InterruptedException ex) {
				log.catching(Level.DEBUG, ex);
				Thread.currentThread().interrupt();
			}
		}

		/**
		 * Will decrease the number of active readers and notify any waiting threads if
		 * necessary.
		 *
		 * @throws IllegalStateException if no readers to unlock
		 */
		@Override
		public void unlock() throws IllegalStateException {
			if (readers > 0) {
				log.debug("Unlocking Read");
				synchronized (lock) {
					if (readers > 0) {
						readers--;
						if (readers == 0) {
							lock.notifyAll();
						}
					}
				}
			} else {
				throw new IllegalStateException();
			}
		}
	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class WriteLock implements SimpleLock {
		/**
		 * Controls access to the write lock. The active thread is forced to wait while
		 * there are any active readers or writers, and it is not the active writer
		 * thread. Once safe, the thread is allowed to acquire a write lock by
		 * incrementing the number of active writers and setting the active writer
		 * reference.
		 */
		@Override
		public void lock() {
			try {
				// Note the lock object being used here and elsewhere
				if (isActiveWriter()) {
					writers++;
					return;
				}

				synchronized (lock) {
					while (readers > 0 || writers > 0) {
						lock.wait();
					}

					writers++;
					activeWriter = Thread.currentThread();

					log.debug("Locked Write");
				}
			}
			catch (InterruptedException ex) {
				log.catching(Level.DEBUG, ex);
				Thread.currentThread().interrupt();
			}
		}

		/**
		 * Will decrease the number of active writers and notify any waiting threads if
		 * necessary. Also unsets the active writer if appropriate.
		 *
		 * @throws IllegalStateException if no writers to unlock
		 * @throws ConcurrentModificationException if there are writers but unlock is
		 *   called by a thread that does not hold the write lock
		 */
		@Override
		public void unlock() throws IllegalStateException, ConcurrentModificationException {
			log.debug("Unlocking Write " + writers + " " + isActiveWriter() + " " + activeWriter);
			synchronized (lock) {
				if (isActiveWriter()) {
					writers--;
					if (writers == 0) {
						activeWriter = null;
						lock.notifyAll();
					}
				} else if (writers > 0 && !isActiveWriter()) {
					throw new ConcurrentModificationException();
				} else {
					throw new IllegalStateException();
				}
			}
		}
	}
}
