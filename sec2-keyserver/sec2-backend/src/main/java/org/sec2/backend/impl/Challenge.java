package org.sec2.backend.impl;

import org.sec2.backend.IChallenge;

public class Challenge implements IChallenge {

    private final byte[] challenge;
    private final long issueTimestamp;
    
    public Challenge(final byte[] challenge, final long issueTimestamp) {
        this.issueTimestamp = issueTimestamp;
        this.challenge = challenge;
    }
    
    @Override
    public byte[] getChallenge() {
        return challenge;
    }

    @Override
    public long getIssueTimestamp() {
        return issueTimestamp;
    }

}
