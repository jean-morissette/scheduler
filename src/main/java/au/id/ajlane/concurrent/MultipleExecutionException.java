/*
 * Copyright 2016 Aaron Lane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.id.ajlane.concurrent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * An {@link ExecutionException} which is caused by multiple tasks failing concurrently.
 * <p/>
 * Useful for implementing {@link java.util.concurrent.ExecutorService#invokeAny(Collection)}.
 */
public class MultipleExecutionException extends ExecutionException
{
    private static final long serialVersionUID = 7735485098344994017L;

    private static Collection<Throwable> nonEmpty(final Collection<Throwable> causes)
    {
        if (causes == null)
        {
            throw new NullPointerException("The collection of causes cannot be null.");
        }
        if (causes.isEmpty())
        {
            throw new IllegalArgumentException("The collection of causes cannot be empty.");
        }
        return causes;
    }

    /**
     * Constructs a new {@code MultipleExecutionException}.
     *
     * @param causes
     *     The causes of the failures. Must not be null or empty.
     */
    public MultipleExecutionException(final Throwable... causes)
    {
        this(Arrays.asList(causes));
    }

    /**
     * Constructs a new {@code MultipleExecutionException}.
     *
     * @param causes
     *     The causes of the failures. Must not be null or empty.
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
