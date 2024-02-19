package concurrent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CustomThreadPool {
    private final int capacity;
    private final Set<Thread> workers;
    private final Queue<Runnable> tasks = new LinkedList<>();
    private volatile boolean isShutDown;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition taskAvailable = lock.newCondition();
    public CustomThreadPool(int capacity) {
        if (capacity > 0) {
            this.capacity = capacity;
            workers = new HashSet<>();
            for (int i = 0; i < capacity; i++) {
                Thread worker = new Worker();
                workers.add(worker);
                worker.start();
            }
        } else throw new IllegalArgumentException("capacity for thread pool must be more than 0");
    }

    public void execute(Runnable task) {
        try {
            lock.lock();
            if (isShutDown) {
                throw new IllegalStateException("CustomThreadPool is already shut down");
            }
            tasks.offer(task);
            taskAvailable.signal();
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        try {
            lock.lock();
            isShutDown = true;
            taskAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void awaitTermination() {
        for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class Worker extends Thread{
        @Override
        public void run() {
            Runnable task;
            while (true) {
                try {
                    lock.lock();
                    while (tasks.isEmpty() && !isShutDown) {
                        try {
                            taskAvailable.await();
                        } catch (InterruptedException e) {
                           return;
                        }
                    }
                    if (isShutDown && tasks.isEmpty()) {
                        return;
                    }
                    task = tasks.poll();
                } finally {
                    lock.unlock();
                }
                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.err.println("Error for execution task: " + e.getCause());
                }
            }
        }

    }
}
