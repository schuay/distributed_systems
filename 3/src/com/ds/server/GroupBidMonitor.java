package com.ds.server;

import com.ds.commands.CommandConfirm;
import com.ds.server.UserList.User;

import java.util.HashSet;
import java.util.Set;

/**
 * The GroupBidMonitor monitors the current state of group bids
 * and can decide to reject a group bid based on deadlock and fairness
 * criteria.
 */
class GroupBidMonitor {
    /* Currently only 3 is supported. */
    public static final int GROUP_SIZE = 3;
    public static final long FAIRNESS_TIME = 300000;

    private final AuctionList auctions;
    private final UserList users;

    private final Set<User> groupBidUsers = new HashSet<User>();

    public GroupBidMonitor(AuctionList auctions, UserList users) {
        this.auctions = auctions;
        this.users = users;
    }

    synchronized void requestConfirmation(CommandConfirm command, GroupBidListener listener,
            User user) {
        int auctionId = command.getAuctionId();
        String bidder = command.getUser();
        int amount = command.getAmount();

        /* Self confirming is forbidden. */
        if (bidder.equals(user.getName())) {
            listener.onRejected();
            return;
        }
        
        /* If there is just one free user remaining... */
        if ((users.numLoggedInUsers() - users.numBlockedUsers()) <= (GROUP_SIZE - 2)) {
            /* ... only allow confirming bids we can finish. */
            if (auctions.getGroupBidNumBidders(auctionId, bidder, amount) < GROUP_SIZE - 1) {
                listener.onRejected();
                return;
            }
        }

        /* TODO: Ensure fairness somehow. */

        if (!auctions.confirmGroupBid(
                command.getAuctionId(),
                command.getUser(),
                command.getAmount(),
                listener)) {
            listener.onRejected();
            return;
        }

        users.blockUser(user);
    }
}
