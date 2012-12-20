package com.ds.server;

import com.ds.commands.CommandConfirm;
import com.ds.server.UserList.User;

import java.util.Date;
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
    //public static final long FAIRNESS_TIME = 300000;
    public static final long FAIRNESS_TIME = 999999999;

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
        
        /* If there is just one free user remaining (the current one is marked blocked too)... */
        if ((users.numLoggedInUsers() - users.numBlockedUsers()) == 0) {
            /* ... only allow confirming bids we can finish. */
            if (auctions.getGroupBidNumBidders(auctionId, bidder, amount) < GROUP_SIZE - 1) {
                long current = new Date().getTime();
                int maxConfirms = users.getMaxAcceptedConfirms(current);
                if (user.getAcceptedConfirms(current) == maxConfirms ||
                    !auctions.rejectGroupBidForConfirm(auctionId, bidder, amount, current, maxConfirms)) {
                    listener.onRejected();
                    return;
                }
            }
        }

        if (!auctions.confirmGroupBid(
                command.getAuctionId(),
                command.getUser(),
                command.getAmount(),
                listener)) {
            listener.onRejected();
            return;
        }
    }
}
