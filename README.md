# Task Description
(This task was given as an assignment in BSc Computer Science - Concurrent Programming course at Eötvös Loránd University)

# Field Race Simulation

In this exercise, you'll simulate a field race that features several checkpoints where the players can collect points.

The following data structures are shared between all.

*   `isOn`: `AtomicBoolean`, initially `true`
*   `scores`: `ConcurrentHashMap`, its key is the player's ID, its value is the player's total score (0 at the outset)
*   `checkpointScores`: an array of `PLAYER_COUNT` elements, `AtomicInteger`s that start out as 0
*   `checkpointQueues`: a synchronized list, contains `CHECKPOINT_COUNT` `BlockingQueue`s that transport `AtomicInteger`s

Class `FieldRace` contains `main` that does the following.

*   It starts `PLAYER_COUNT + CHECKPOINT_COUNT + 1` threads using an `ExecutorService`.
    
    *   Thread "+1" prints the scores of all players in order in a format like this every second: `Scores: [1=494, 8=473, 4=456, 9=445, 2=431, 3=430, 5=368, 7=367, 6=360, 0=353]`
        *   If `isOn` is `false`, the thread terminates.
    *   See details about the other threads below.
*   After 10 seconds, `main` sets `isOn` to false, and stops the `ExecutorService` in the following way.
    
        ex.shutdown();
        ex.awaitTermination(3, TimeUnit.SECONDS);
        ex.shutdownNow();
        
    
*   Finally, `main` prints the total score one last time.
    
*   At this time, the program has to fully stop. If it doesn't, investigate whether one or more threads are blocked.
    

Player and checkpoint threads will keep repeating their activities as long as `isOn` is `true`.

The repeated activity of a checkpoint is the following.

*   It takes an element out of the appropriate element of the `checkpointQueues` list. If the element does not show up in 2 seconds, it starts a new iteration.
*   It writes a random value between 10 and 100 into the received `AtomicInteger` element.
*   Using the received `AtomicInteger` element, it notifies the player that it just received a score at the checkpoint.

The repeated activity of a player is the following.

*   It randomly chooses a checkpoint.
*   It does nothing for a random amount of time (between 0.5 and 2 seconds), which simulates the time it takes for the player to get to the checkpoint.
*   The appropriate element of `checkpointScores` (let's call it `checkpointScore`) is entered into the `BlockingQueue` of the checkpoint.
*   Using `checkpointScore`, it waits until the checkpoint notifies it about the score.
    *   After the notification wakes the player up, if `checkpointScore` remains 0, start waiting again.
        *   Proceed in exactly this way if no notification arrives within 3 seconds.
    *   If `isOn` is set to `false`, it immediately finishes execution.
*   It sets the value of `checkpointScore` back to 0.
*   It prints a message about the received score (`Player 2 got 50 points at checkpoint 8`), and increases the player's score in `scores`.
