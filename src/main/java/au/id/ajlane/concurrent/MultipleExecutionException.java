package au.id.ajlane.concurrent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * An {@link ExecutionException} which is caused by multiple tasks failing concurrently.
 * <p>
 * Useful for implementing {@link java.util.concurrent.ExecutorService#invokeAny(Collection)}.
 */
public class MultipleExecutionException extends ExecutionException
{
    private static final long serialVersionUID = 7735485098344994017L;

    private static Collection<Throwable> nonEmpty(final Collection<Throwable> causes)
    {
        if (causes == null) { throw new NullPointerException("The collection of causes cannot be null."); }
        if (causes.isEmpty()) { throw new IllegalArgumentException("The collection of causes cannot be empty."); }
        return causes;
    }

    /**
     * Constructs a new {@code MultipleExecutionException}.
     *
     * @param causes
     *         The causes of the failures. Must not be null or empty.
     */
    public MultipleExecutionException(final Throwable... causes)
    {
        this(Arrays.asList(causes));
    }

    /**
     * Constructs a new {@code MultipleExecutionException}.
     *
     * @param causes
     *         The causes of the failures. Must not be null or empty.
     */
    public MultipleExecutionException(final Collection<Throwable> causes)
    {
        this(nonEmpty(causes).size(), causes.iterator());
    }

    private MultipleExecutionException(final int count, final Iterator<Throwable> causes)
    {
        super(count + " concurrent tasks failed.", causes.next());

        while (causes.hasNext())
        {
            addSuppressed(causes.next());
        }
    }
}
