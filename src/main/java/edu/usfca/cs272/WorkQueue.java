package edu.usfca.cs272;

import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple work queue implementation based on the IBM developerWorks article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href=
 *   "https://web.archive.org/web/20210126172022/https://www.ibm.com/developerworks/library/j-jtp0730/index.html">
 *   Java Theory and Practice: Thread Pools and Work Queues</a>
 *
 * @author Honghuai(King) Ke
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class WorkQueue {
    /** Workers that wait until work (or tasks) are available. */
    private final Worker[] workers;

    /** Queue of pending work (or tasks). */
    private final LinkedList<Runnable> tasks;

    /** Used to signal the workers should terminate. */
    private volatile boolean shutdown;

    /** The default number of worker threads to use when not specified. */
    public static final int DEFAULT = 5;

    /** Logger used for this class. */
    private static final Logger log = LogManager.getLogger();

    /** Used to track unfinished work */
    private int pending;

    /**
     * Starts a work queue with the default number of threads.
     *
     * @see #WorkQueue(int)
     */
    public WorkQueue() {
        this(DEFAULT);
    }

    /**
     * Starts a work queue with the specified number of threads.
     *
     * @param threads number of worker threads; should be greater than 1
     */
    public WorkQueue(int threads) {
        this.tasks = new LinkedList<Runnable>();
        this.workers = new Worker[threads];
        this.shutdown = false;
        this.pending = 0;

        // start the threads so they are waiting in the background
        for (int i = 0; i < threads; i++) {
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    /**
     * Adds a work (or task) request to the queue. A worker thread will process this
     * request when available.
     *
     * @param task work request (in the form of a {@link Runnable} object)
     */
    public void execute(Runnable task) {
        synchronized (tasks) {
            // TODO tasks.addLast(task) (文档内看2）
            tasks.addLast(() -> {
                try {
                    task.run();
                } finally {
                   notifyWorkers(true);
                }
            });

            notifyWorkers(false);
        }
    }

    /**
     * Notifies all worker threads about changes in the state of the task queue.
     * This method is called whenever a new task is added to the queue or a task is completed.
     * It is responsible for incrementing or decrementing the count of pending tasks
     * and waking up threads that are waiting for tasks to be completed.
     *
     * @param isTaskFinished Indicates whether a task has just been completed. If true,
     *                       it decrements the count of pending tasks. If false, it
     *                       increments the count, signifying the addition of a new task.
     */
    private void notifyWorkers(boolean isTaskFinished) {
        synchronized (tasks) {
            if (isTaskFinished) {
                pending--;
            } else {
                pending++;
            }

            tasks.notifyAll();  // Notify any threads waiting in finish()
        }
    }

    /**
     * Waits for all pending work (or tasks) to be finished. Does not terminate the
     * worker threads so that the work queue can continue to be used.
     */
    public void finish() {
        synchronized (tasks) {
            while (pending > 0) {  // wait until no more pending tasks
                try {
                    tasks.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Similar to {@link Thread#join()}, waits for all the work to be finished and
     * the worker threads to terminate. The work queue cannot be reused after this
     * call completes.
     */
    public void join() {
        try {
            finish();
            shutdown();

            for (Worker worker : workers) {
                worker.join();
            }
        }
        catch (InterruptedException e) {
            System.err.println("Warning: Work queue interrupted while joining.");
//          log.catching(Level.WARN, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Asks the queue to shutdown. Any unprocessed work (or tasks) will not be
     * finished, but threads in-progress will not be interrupted.
     */
    public void shutdown() {
        // safe to do unsynchronized due to volatile keyword
        shutdown = true;

        synchronized (tasks) {
            tasks.notifyAll();
        }
    }

    /**
     * Returns the number of worker threads being used by the work queue.
     *
     * @return number of worker threads
     */
    public int size() {
        return workers.length;
    }

    /**
     * Waits until work (or a task) is available in the work queue. When work is
     * found, will remove the work from the queue and run it.
     *
     * <p>
     * If a shutdown is detected, will exit instead of grabbing new work from the
     * queue. These threads will continue running in the background until a shutdown
     * is requested.
     */
    private class Worker extends Thread {
        /**
         * Initializes a worker thread with a custom name.
         */
        public Worker() {
            setName("Worker" + getName());
        }

        @Override
        public void run() {
            Runnable task = null;

            try {
                while (true) {
                    synchronized (tasks) {
                        while (tasks.isEmpty() && !shutdown) {
                            tasks.wait();
                        }

                        // exit while for one of two reasons:
                        // (a) queue has work, or (b) shutdown has been called

                        if (shutdown) {
                            break;
                        }

                        task = tasks.removeFirst();
                    }

                    try {
                        task.run();
                    }
                    catch (RuntimeException e) {
                        // catch runtime exceptions to avoid leaking threads
                        System.err.printf("Error: %s encountered an exception while running.%n", this.getName());
                        log.error(e);
                    }
                }
            }
            catch (InterruptedException e) {
                // causes early termination of worker threads
                System.err.printf("Warning: %s interrupted while waiting.%n", this.getName());
                log.error(e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
