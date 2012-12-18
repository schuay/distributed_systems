package com.ds.server;

import com.ds.commands.CommandConfirm;

/**
 * The GroupBidMonitor monitors the current state of group bids
 * and can decide to reject a group bid based on deadlock and fairness
 * criteria.
 */
class GroupBidMonitor {

    private final AuctionList auctions;

    public GroupBidMonitor(AuctionList auctions) {
        this.auctions = auctions;
    }

    void requestConfirmation(CommandConfirm command, GroupBidListener listener) {

        /* TODO: Decide whether to confirm or reject the bid. */

        auctions.confirmGroupBid(
                command.getAuctionId(),
                command.getUser(),
                command.getAmount(),
                listener);
    }
}
