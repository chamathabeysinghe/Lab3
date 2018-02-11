import java.util.concurrent.locks.ReentrantLock;

public class Main {


    public static Semaphore busSemaphore;
    public static Semaphore boardedSemaphore;
    public static ReentrantLock mutex;

    public static void main(String args[]) {
        busSemaphore = new Semaphore(0);
        boardedSemaphore = new Semaphore(0);
        mutex = new ReentrantLock();

        for(int i = 0;i<100;i++){
            new Passenger(i).start();
        }
        new Bus(99).run();
        new Bus(100).run();
        new Bus(101).run();

    }

}

class Semaphore {

    int val;

    Semaphore(int val) {
        this.val = val;
    }

    public synchronized void up() {
        val++;
        this.notify();
    }

    public synchronized void down() {
        val--;
        if (val < 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}

class Bus extends Thread {


    public static Bus currentBus = null;
    public static int busId;

    Bus(int busId) {
        this.busId = busId;
    }

    private void busArrived() {
        System.out.println("Bus: " + busId + " arrived");
    }

    private void busDeparted() {
        System.out.println("Bus: " + busId + " departed");
    }

    public void run() {

        Main.mutex.lock();
        Bus.currentBus = this;
        busArrived();


        int n = Math.min(Passenger.waiting, 50);

        for (int i = 0; i < n; i++) {
            Main.busSemaphore.up();
            Main.boardedSemaphore.down();
        }
        Passenger.waiting = Math.max(Passenger.waiting - 50, 0);
        busDeparted();
        Main.mutex.unlock();

    }

}

class Passenger extends Thread {


    public static int waiting = 0;
    private int passengerId;

    Passenger(int passengerId) {
        this.passengerId = passengerId;
    }


    private void board() {
        System.out.println("Passenger: " + passengerId + " boarded to bus: " + Bus.currentBus.busId);
    }

    public void run() {
        Main.mutex.lock();
        Passenger.waiting += 1;
        Main.mutex.unlock();
        System.out.println("Passenger: "+passengerId+" arrived");
        Main.busSemaphore.down();
        board();
        Main.boardedSemaphore.up();

    }
}