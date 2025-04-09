package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier {
    private final int parties;
    private final ReentrantLock lock;
    private final Condition condition;
    private int cycleNumber = 0;
    private int waitingParties = 0;

    public Barrier(final int parties) {
        this.parties = parties;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    public void await() throws InterruptedException {
        lock.lock();
        int currentCycle = cycleNumber;
        waitingParties++;
        if (waitingParties == parties) {
            waitingParties = 0;
            cycleNumber++;
            condition.signalAll();
        } else {
            while (currentCycle == cycleNumber) {
                condition.await();
            }
        }
        lock.unlock();
    }
}
