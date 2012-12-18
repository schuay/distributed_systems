
package com.ds.responses;

import com.ds.server.UserList;
import com.ds.util.Multiline;

public class ResponseClientList extends Response {

    private static final long serialVersionUID = 1L;

    private final String clientList;

    public ResponseClientList(UserList list) {
        super(Rsp.CLIENT_LIST);
        this.clientList = list.toString();
    }

    public ResponseClientList(String list) {
        super(Rsp.CLIENT_LIST);
        this.clientList = Multiline.decode(list);
    }

    public String getClientList() {
        return clientList;
    }

    @Override
    public String toNetString() {
        return String.format("%s %s", super.toNetString(), Multiline.encode(clientList));
    }

    @Override
    public String toString() {
        return clientList;
    }
}
