/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.util;

/**
 * Enumeration representing a vote. Votes are often used to determine the result
 * of an event preview.
 */
public enum Vote {
    /**
     * Represents an approval vote.
     */
    APPROVE,

    /**
     * Represents a denial vote.
     */
    DENY,

    /**
     * Represents a deferred vote, implying that the vote will be approved later.
     */
    DEFER;

    /**
     * Tally a new vote against the previous tally result.
     * For a vote tally to work correctly the initial value must be {@link #APPROVE}.
     * <p>The tallying algorithm is as follows:
     * <ul>
     * <li>New vote is {@link #APPROVE} {@code ->} result is previous result.
     * <li>New vote is {@link #DENY} {@code ->} result is new vote (that is, <code>DENY</code>).
     * <li>New vote is {@link #DEFER} {@code ->} result is <code>DENY</code> if previous was
     * <code>DENY</code>, otherwise <code>DEFER</code>.
     * </ul>
     *
     * @param vote The new vote to tally.
     * @return The result of tallying this new vote against this
     * previously accumulated result.
     * @throws IllegalArgumentException if the new vote is {@code null}.
     */
    public Vote tally(final Vote vote) {
        Utils.checkNull(vote, "vote");

        Vote tally;

        switch (vote) {
            case APPROVE:
                tally = this;
                break;

            case DENY:
                tally = vote;
                break;

            case DEFER:
                tally = (this == DENY) ? this : vote;
                break;

            default:
                throw new IllegalArgumentException("Unknown Vote value to tally.");
        }

        return tally;
    }
}
