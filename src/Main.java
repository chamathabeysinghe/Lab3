import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;


/**
 * Solution to Senate Bus Problem
 */

public class Main {


    public static Semaphore busSemaphore;
    public static Semaphore boardedSemaphore;
    public static ReentrantLock mutex;
    private static Random rand = new Random(); // generates uniformly random numbers

    public static void main(String args[]) {
        busSemaphore = new Semaphore(0);
        boardedSemaphore = new Semaphore(0);
        mutex = new ReentrantLock();

        /**
         * Add passengers to the queue
         * at the bus stop.
         * @See #getRandomNumber for sleep time calculation.
         */
        Thread passengerDaemon = new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while(true){
                    try {
                        int sleepTime = Main.getRandomNumber(5); // means modified to reflect given ratio (40:1)
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
                        Thread.sleep(Main.getRandomNumber(200)); // means modified to reflect given ratio (40:1)
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

    /**
     *  x = log(1-u)/(−λ)
     *  where u is a uniform random number between 0 and 1,
     *  λ is the rate parameter,
     *  and x is the random number with an exponential distribution.
     *
     *  Using inverse transform sampling method to get values from exponential distribution,
     *  modified to use mean directly, which is inverse of rate parameter.
     *  Multiplied by 100 to get usable millisecond values. */
    public static int getRandomNumber(int mean){
        return (int) (Math.log(1- rand.nextDouble()) * 100 * mean * (-1));
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

        /* stop new passengers from entering queue
         while existing passengers are boarding. */
        Main.mutex.lock();
        Bus.currentBus = this;
        busArrived();

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


