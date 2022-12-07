import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.*;
public class FieldRace
{
    final static int PLAYER_COUNT = 12;
    final static int CHECKPOINT_COUNT = 5;

    public static AtomicBoolean isOn = new AtomicBoolean(true);
    public static ConcurrentHashMap<String,Integer> scores = new ConcurrentHashMap<>();
    public static ArrayList<AtomicInteger> checkpointScores = new ArrayList<AtomicInteger>(PLAYER_COUNT);
    public static List<ArrayBlockingQueue<AtomicInteger>> checkpointQueues = Collections.synchronizedList(new ArrayList<ArrayBlockingQueue<AtomicInteger>>(CHECKPOINT_COUNT));

    public static class CheckPoint extends Thread
    {
        public CheckPoint(String name) 
        {
            this.setName(name);
        }
        public void run()
        {
          while(isOn.get())
           {
                int a = Integer.parseInt((getName().split(" "))[1]);
                BlockingQueue<AtomicInteger> apnaCheckPoint = checkpointQueues.get(a);
                try
                {
                    int rand =ThreadLocalRandom.current().nextInt(10, 101);
                    AtomicInteger acquired = apnaCheckPoint.take();
                    acquired.set(rand);
                    synchronized(acquired)
                    {
                        acquired.notify();
                    }
                }
                catch (InterruptedException e) 
                {
                }
           }
        }
    }

    public static class Player extends Thread
    {
        public Player(String name) 
        {
            this.setName(name);
        }
        public void run()
        {
           while(isOn.get())
           {
                int a = Integer.parseInt((getName().split(" "))[1]);
                int rand =ThreadLocalRandom.current().nextInt(0, CHECKPOINT_COUNT);
                BlockingQueue<AtomicInteger> apnaCheckPoint = checkpointQueues.get(rand);
                try{
                    Thread.sleep(1500);
                    AtomicInteger checkpointScore = checkpointScores.get(a);
                    apnaCheckPoint.put(checkpointScore);
                    while(checkpointScore.get() == 0)
                    {
                        synchronized(checkpointScore)
                        {
                            checkpointScore.wait(3000);
                        }
                    }

                    if(scores.containsKey(Integer.toString(a)))
                    {
                        int haha = scores.get(Integer.toString(a));
                        scores.put(Integer.toString(a),haha+checkpointScore.get());
                    }
                    else
                    {
                        scores.put(Integer.toString(a),checkpointScore.get());
                    }
                    System.out.println(getName() + " got "+checkpointScore.get()+" points at checkpoint "+(rand) );
                    checkpointScore.set(0);
                }
                catch (InterruptedException e){}
           }
        }
    }

    public static void main(String args[])
    {
        for(int i = 0;i<CHECKPOINT_COUNT;i++)
        {
            checkpointQueues.add(new ArrayBlockingQueue<AtomicInteger>(CHECKPOINT_COUNT+1));
        }
        for(int i = 0;i<PLAYER_COUNT;i++)
        {
            checkpointScores.add(new AtomicInteger());
        }

        ExecutorService pool = Executors.newFixedThreadPool(PLAYER_COUNT+CHECKPOINT_COUNT+1);
        for(int i =0;i<PLAYER_COUNT;i++)
        {
              pool.submit(new Player("Player "+i));
        }
        for(int i =0;i<CHECKPOINT_COUNT;i++)
        {
              pool.submit(new CheckPoint("CheckPoint "+i));
        }

        pool.submit(() -> {
            try{
                Thread.sleep(1000);
                while(true)
                {
                    Thread.sleep(1000);
                    System.out.println("Scores :- "+scores);
                }
            }
            catch (InterruptedException e) {}
        });
        try{
            Thread.sleep(10000);
            isOn.set(false);
        }
        catch (InterruptedException e){}
        pool.shutdown();
        try{
        pool.awaitTermination(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException e){}
        pool.shutdownNow();
        System.out.println("Final Scores :- " +scores);
        //System.out.println(scores.size());
    }
}