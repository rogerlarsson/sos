package uk.ac.standrews.cs.sos.protocol;

import uk.ac.standrews.cs.LEVEL;
import uk.ac.standrews.cs.sos.constants.Threads;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton pattern used for this class.
 *
 * TODO - add ability to prioritise tasks
 * TODO - ability to persist tasks -- tasks must be "describable"
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class TasksQueue {

    private ScheduledExecutorService executorService;

    private static TasksQueue instance;
    private TasksQueue() {
        executorService = Executors.newScheduledThreadPool(Threads.TASKS_SCHEDULER_PS);
    }

    public static TasksQueue instance() {
        if (instance == null) {
            instance = new TasksQueue();
        }

        return instance;
    }

    public void performSyncTask(Task task) {

        try {
            synchronized (task) {
                performAsyncTask(task);

                task.wait();
                SOS_LOG.log(LEVEL.INFO, "TasksQueue :: Task finished " + task);
            }
        } catch (InterruptedException e) {
            SOS_LOG.log(LEVEL.ERROR, "TasksQueue :: " + e.getMessage());
        }

    }

    public void performAsyncTask(Task task) {

        SOS_LOG.log(LEVEL.INFO, "TasksQueue :: Submitting task " + task);

        final Future handler = executorService.submit(task);
        executorService.schedule(() -> {
            handler.cancel(true);
            SOS_LOG.log(LEVEL.WARN, "TasksQueue :: Cancelled task " + task);

            task.notify();
        }, 30, TimeUnit.SECONDS);

        SOS_LOG.log(LEVEL.INFO, "TasksQueue :: Task submitted " + task);
    }
}
