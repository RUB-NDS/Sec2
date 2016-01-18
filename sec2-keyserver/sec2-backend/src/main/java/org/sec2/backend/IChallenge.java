package org.sec2.backend;

/**
 * Interface to describe a challenge.
 * 
 * @author Utimaco Safeware
 */
public interface IChallenge {
    /**
     * Returns the challenge
     * @return byte array representing the challenge
     */
    byte[] getChallenge();
    /**
     * Returns the time stamp when the challenge was issued. 
     * @return
     */
    long getIssueTimestamp();
}
