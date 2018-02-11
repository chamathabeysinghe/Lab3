import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;


/**
 * Solution to Senate Bus Problem
 *
 * Assumptions
 *   * 50 passengers are able to board a bus
 *     before the next bus arrives (20 seconds).
 */

public class Main {


    public static Semaphore busSemaphore;
    public static Semaphore boardedSemaphore;
    public static ReentrantLock mutex;

    public static void main(String args[]) {
        busSemaphore = new Semaphore(0);
        boardedSemaphore = new Semaphore(0);
        mutex = new ReentrantLock();

        /**
         * Add passengers to the queue
         * at the bus stop
         */
        Thread passengerDaemon = new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                Random randomGenerator = new Random();
                while(true){
                    try {
                        int sleepTime = randomGenerator.nextInt(800); // passengers arrive randomly.
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Passenger(count).start();
                    count += 1;

                }
            }
        });

        /**
         * Bring buses to bus stop.
         */
        Thread busDaemon = new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while(true){
                    try {
                        Thread.sleep(20000); // buses come every 20 seconds.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Bus(count).start();
                    count += 1;

                }
            }
        });

        passengerDaemon.start();
        busDaemon.start();


    }

}


class Bus extends Thread {


    public static Bus currentBus = null;
    public static int busId;

    Bus(int busId) {
        this.busId = busId;
    }

    private void busArrived() {
        System.out.println("Bus: " + busId + " arrived.");
    }

    private void busDeparted() {
        System.out.println("Bus: " + busId + " departed.");
    }

    public void run() {

        Bus.currentBus = this;
        busArrived();

        /* stop new passengers from entering queue
         while existing passengers are boarding. */
        Main.mutex.lock();

        /* to either take all waiting, or up to 50 passengers */
        int n = Math.min(Passenger.waiting, 50);

        for (int i = 0; i < n; i++) {
            Main.busSemaphore.release(); // allow passenger in.
            try {
                Main.boardedSemaphore.acquire(); // wait until passengers board
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Passenger.waiting = Math.max(Passenger.waiting - 50, 0);
        Main.mutex.unlock(); // allow new passengers into a new queue

        busDeparted();

    }

}

class Passenger extends Thread {


    public static int waiting = 0;
    private int passengerId;

    Passenger(int passengerId) {
        this.passengerId = passengerId;
    }


    private void board() {
        System.out.println("Passenger: " + passengerId + " boarded to bus: " + Bus.currentBus.busId + ".");
    }

    public void run() {
        /* before entering the waiting queue */
        Main.mutex.lock();
        Passenger.waiting += 1;
        Main.mutex.unlock();
        System.out.println("Passenger: " + passengerId + " arrived.");

        try {
            Main.busSemaphore.acquire(); // wait for bus.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        board();
        Main.boardedSemaphore.release(); // notify bus that passenger boarded.

    }
}


