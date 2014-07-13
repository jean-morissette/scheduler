package au.id.ajlane.concurrent;

import java.util.concurrent.Executor;

/**
 * An {@link Executor} that executes all tasks immediately in the calling thread.
 */
@SuppressWarnings("Singleton")
public final class SameThreadExecutor implements Executor
{
    private static final SameThreadExecutor INSTANCE = new SameThreadExecutor();

    /**
     * Gets the single shared {@code SameThreadExecutor} instance.
     *
     * @return An instance of {@code SameThreadExecutor}.
     */
    public static SameThreadExecutor get()
    {
        return INSTANCE;
    }

    private SameThreadExecutor()
    {
    }

    @Override
    public void execute(final Runnable command)
    {
        command.run();
    }
}
