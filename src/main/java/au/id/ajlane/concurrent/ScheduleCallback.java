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

/**
 * A function which can provide the time to schedule the next in a series of tasks.
 *
 * @param <V>
 *     The type of the value returned by the scheduled action.
 *
 * @see CalendarExecutorService
 */
@FunctionalInterface
public interface ScheduleCallback<V>
{
    /**
     * Get the time to schedule the next action.
     *
     * @param previousInstant
     *     The scheduled time of the previous task in the series.
     * @param previousValue
     *     The return value of the previous task in the series. {@code null} if the task does not have a return value.
     *
     * @return An {@link java.time.Instant}. If the time has already passed, then the action should be scheduled to
     * occur as soon as possible. May not be {@code null}.
     */
    Instant getNext(final Instant previousInstant, final V previousValue);
}
