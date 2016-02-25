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

import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledFuture;

/**
 * A result-bearing, cancellable action which is scheduled to occur at a particular time.
 * <p>
 * All instances of {@code CalendarFuture} are also instances of {@link CompletionStage}.
 *
 * @see CalendarExecutorService
 */
public interface CalendarFuture<V> extends ScheduledFuture<V>, CompletionStage<V>
{
    /**
     * Gets the instant that the action was scheduled for.
     *
     * @return An instant.
     */
    Instant getInstant();
}
