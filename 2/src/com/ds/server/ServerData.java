
package com.ds.server;

public class ServerData {

    private final UserList userList = new UserList();

    public UserList getUserList() {
        return userList;
    }

    private final AuctionList auctionList = new AuctionList();

    public AuctionList getAuctionList() {
        return auctionList;
    }
}
