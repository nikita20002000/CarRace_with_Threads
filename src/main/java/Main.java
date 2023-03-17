import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Novikov Nikita 17.03.2023 Race_Task
 */


import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Novikov Nikita ${DATE} $PROJECT_NAME
 */
public class Main {
    private static final int carsCountInTunnel = 3;

    private static final int carCount = 10;
    private static final ExecutorService executorServise = Executors.newFixedThreadPool(carCount);
    private static final CyclicBarrier cyclicBarrier = new CyclicBarrier(carCount);

    private static final Map<Integer, Long> score = new ConcurrentHashMap<>();  //для того, чтобы работать в потокобезопасной коллекции с результатами гонки

    private static int winnerIndex = -1;

    private static final Object monitor = new Object();

    private static final CountDownLatch countDownLatch = new CountDownLatch(carCount);   //чтобы дождаться заверешения всех потоков(авто)

    private static final Semaphore tunnelSemaphore = new Semaphore(carsCountInTunnel);  //создание семафора
    public static void main(String[] args) {

        for (int i = 0; i < carCount; i++){
            final int index = i;
            executorServise.execute(new Runnable() {
                @Override
                public void run() {
                    prepareToRace(index);
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    long before = System.currentTimeMillis();
                    FirstRoad(index);
                    tunnel(index);
                    SecondRoad(index);
                    synchronized (monitor) {
                        if (winnerIndex == -1) {
                            winnerIndex = index;
                        }
                    }
                    long after = System.currentTimeMillis();

                    score.put(index, after - before);
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int key : score.keySet()){
            System.out.println(key + " - " + score.get(key));
        }
        System.out.println("winner - " +  winnerIndex + " Time - " + score.get(winnerIndex));
    }



    private static void SleepRandomTime() {   //метод, который усыпляет потоки на рандомное время
        long millis = (long) (Math.random() * 5000 + 1000);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    private static void prepareToRace(int index){
        System.out.println(index + " started preparing to Race");
        SleepRandomTime();
        System.out.println(index + " finished preparing");
    }

    private static void FirstRoad(int index){
        System.out.println(index + " started first road");
        SleepRandomTime();
        System.out.println(index + " finished first road");
    }

    private static void SecondRoad(int index){
        System.out.println(index + " started second road");
        SleepRandomTime();
        System.out.println(index + " finished second road");
    }

    private static void tunnel(int index){                      //в туннеле может быть максимум 3 машины, используем семафор
        try {
            tunnelSemaphore.acquire();
            System.out.println(index + " started tunnel");
            SleepRandomTime();
            System.out.println(index + " finished tunnel");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            tunnelSemaphore.release();
        }
    }
}

