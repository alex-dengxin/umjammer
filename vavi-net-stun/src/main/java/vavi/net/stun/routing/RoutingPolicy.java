/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.routing;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import vavi.net.stun.Node;
import vavi.net.stun.NodeInfo;
import vavi.net.stun.messaging.ID;
import vavi.net.stun.messaging.Message;
import vavi.net.stun.network.Connection;
import vavi.net.stun.network.Server;
import vavi.net.stun.stun.STUNCheckServer;
import vavi.net.stun.stunresolver.ConnectionResolver;
import vavi.net.stun.stunresolver.DatagramConnection;
import vavi.net.stun.stunresolver.DatagramConnectionEventHandler;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * RoutingPolicy.
 *
 * @author suno
 * Created on 2003/07/01
 */
public abstract class RoutingPolicy
    implements DatagramConnectionEventHandler, MessageEventHandler {

    /** */
    private Node node;

    /** */
    private STUNCheckServer.StunType stunType;

    /** */
    private ConnectionResolver resolver;

    /** */
    public RoutingPolicy(Node node, STUNCheckServer.StunType stunType) {
        this.node = node;
        this.stunType = stunType;
        this.resolver = new ConnectionResolver(this);
    }

    /** */
    public STUNCheckServer.StunType getLocalSTUNType() {
        return stunType;
    }

    /** */
    public ID getLocalID() {
        return node.getID();
    }

    /** */
    public Node getNodeInterface() {
        return node;
    }

    /** */
    public static RoutingPolicy getRoutingPolicy(Node node, NodeInfo[] servers, STUNCheckServer.StunType stunType) {
        if (stunType.equals(STUNCheckServer.StunType.GLOBAL)) {
            return new BasicRoutingPolicy(node, servers, stunType);
        } else {
            return new ChildNodeRoutingPolicy(node, servers, stunType);
        }
    }

    /** */
    public void onDatagramConnectionEstablished(DatagramConnection connection) {
        node.onDatagramConnectionEstablished(connection);
    }

    /** */
    public void onDatagramConnectionFailed(ID id) {
        node.onDatagramConnectionFailed(id);
    }

    /** */
    public abstract ID getServerIDAtRandom();

    /** */
    public abstract void sendMessage(Message message) throws IOException;

    /** */
    public void getDirectConnection(ID id) throws IOException {
        resolver.connectTo(id);
    }

    /** */
    public abstract NodeInfo[] getAllNodeInfo();

    /** */
    public abstract NodeInfo getLocalNodeInfo();
}

/* */


/**
 * ChildNodeRouting.
 */
class ChildNodeRoutingPolicy extends RoutingPolicy {
    /** */
    private Connection serverConnection;

    /** */
    private NetworkMessageBridge bridge;

    /** */
    private NodeInfo[] serverInfos;

    /** */
    private ConnectionResolver resolver;

    /** */
    public ChildNodeRoutingPolicy(Node node, NodeInfo[] servers, STUNCheckServer.StunType stunType) {
        super(node, stunType);
        this.serverInfos = servers;
        this.bridge = new NetworkMessageBridge(this);
        this.resolver = new ConnectionResolver(this);
        connectToServer(serverInfos);
    }

    /** */
    public ID getServerIDAtRandom() {
        throw new UnsupportedOperationException("don't call this method!!");
    }

    /**
     * @throws IllegalStateException サーバノードが一つも存在しない場合
     */
    private void connectToServer(NodeInfo[] servers) {
        for (NodeInfo server : servers) {
            try {
                serverConnection = new Connection(bridge, server.getIP(), server.getTCPConnectionPort(), Connection.Type.CHILD);
                serverConnection.setName("ChildNodeRoutingPolicy: " + serverConnection);
                return;
            } catch (IOException e) {
                Debug.println(server + " is not alive.");
            }
        }
        throw new IllegalStateException("couldn't connect to a server node!!!");
    }

    /** */
    public void onConnectionEstablished(Connection connection, ID id, NodeInfo nodeInfo) throws IOException {
        if (connection.getConnectionType().equals(Connection.Type.SERVER)) {
            connection.close();
            return;
        }
        connection.start();
    }

    /** */
    public void onDisconnect(Connection aConnection) {
        // /implement!!
        // needs to implement recovery method;
    }

    /** */
    public void onMessageArrived(Connection connection, Message message) {
Debug.println("new message!!:\n" + StringUtil.getDump(message.getData()));
        if (message.isControlMessage()) {
            resolver.onControlMessageArrived(message);
        } else if (message.isGeneralMessage()) {
            getNodeInterface().onMessageArrived(message);
        }
    }

    /** */
    public void sendMessage(Message message) throws IOException {
//Debug.println(message);
        serverConnection.write(message.toBytes());
    }

    /** */
    public NodeInfo[] getAllNodeInfo() {
        return serverInfos;
    }

    /** */
    public NodeInfo getLocalNodeInfo() {
        try {
            return new NodeInfo(InetAddress.getLocalHost(), 0, 0, 0, 0);
        } catch (UnknownHostException e) {
            throw (RuntimeException) new IllegalStateException("may not occur").initCause(e);
        }
    }
}

/* */


/**
 * グローバルアドレスで起動したノードで使用されるルーティングポリシークラスです。
 *
 * BasicRoutingPolicy.
 */
class BasicRoutingPolicy extends RoutingPolicy
    implements MessageEventHandler {

    /** */
    private Map<ID, Connection> serverNodes = new HashMap<ID, Connection>();

    /** */
    private Map<ID, Connection> childNodes = new HashMap<ID, Connection>();

    /** */
    private NetworkMessageBridge bridge;

    /** */
    private Server localServer;

    /** */
    private Map<Connection, NodeInfo> serverInfos = new HashMap<Connection, NodeInfo>();

    /** */
    private NodeInfoServer nodeInfoServer;

    /** */
    private STUNCheckServer stunServer;

    /** */
    private ConnectionResolver resolver;

    /** */
    private static final int DEFAULTPORT = 2000;

    /**
     * @throws IllegalStateException could not bind any port
     */
    public BasicRoutingPolicy(Node node, NodeInfo[] servers, STUNCheckServer.StunType stunType) {
        super(node, stunType);
Debug.println("test: " + servers.length);
        NodeInfo[] someNodeInfo = servers;
        this.bridge = new NetworkMessageBridge(this);
        try {
            this.localServer = new Server(bridge, DEFAULTPORT);
        } catch (IOException e) {
Debug.println("could not bind default port(" + DEFAULTPORT + "), trying other port");
            try {
                this.localServer = new Server(bridge, 0);
            } catch (IOException f) {
                throw (RuntimeException) new IllegalStateException("could not bind any port").initCause(f);
            }
        }
        localServer.setName("BasicRoutingPolicy");
        localServer.start();

        this.nodeInfoServer = new NodeInfoServer(this);
        this.stunServer = new STUNCheckServer(this);
        this.resolver = new ConnectionResolver(this);

        // connect to all other servers
        for (int i = 0; i < someNodeInfo.length; i++) {
            try {
                Connection connection = new Connection(bridge, someNodeInfo[i].getIP(), someNodeInfo[i].getTCPConnectionPort(), Connection.Type.SERVER);
                connection.setName("BasicRoutingPolicy[" + i + "]: " + connection);
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        }
        serverInfos.put(null, getLocalNodeInfo());
    }

    /** */
    private void sendToSelectedHosts(Map<ID, Connection> hosts, byte[] data) {
        for (Connection connection : hosts.values()) {
            try {
Debug.println("sending a message to a host");
                connection.write(data);
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        }
    }

    /** */
    public void sendToAllServers(byte[] data) {
        sendToSelectedHosts(serverNodes, data);
    }

    /** */
    public void sendToAllChildren(byte[] data) {
        sendToSelectedHosts(childNodes, data);
    }

    /** */
    public void sendToAllHosts(byte[] data) {
        sendToAllServers(data);
        sendToAllChildren(data);
    }

    /** */
    private void sendToAllHostsExcept(Collection<Connection> connections, Connection exceptConnection, byte[] data) {
        for (Connection connection : connections) {
            if (connection != exceptConnection) {
                try {
                    connection.write(data);
                } catch (IOException e) {
                    Debug.printStackTrace(e);
                }
            }
        }
    }

    /** */
    public void sendToAllHostsExcept(Connection exceptConnection, byte[] data) {
        sendToAllHostsExcept(serverNodes.values(), exceptConnection, data);
        sendToAllHostsExcept(childNodes.values(), exceptConnection, data);
    }

    /** */
    public void onMessageArrived(Connection connection, Message message) throws IOException {
Debug.println("message arrived!!");

        if (message.getDestination().equals(ID.BROADCASTID)) {
            if (childNodes.containsValue(connection)) {
                // if it's from a child, send it to every one except the source.
Debug.println("sending to all except...");
                sendToAllHostsExcept(connection, message.toBytes());
            } else if (serverNodes.containsValue(connection)) {
                // if it's from a server, just send it to the children.
                sendToAllChildren(message.toBytes());
            }
        } else if (!message.getDestination().equals(getLocalID())) {
            if (childNodes.containsKey(message.getDestination())) {
                // to one of the children
                childNodes.get(message.getDestination()).write(message.toBytes());
            } else if (childNodes.containsValue(connection)) {
                // from one of the children
                sendToAllServers(message.toBytes());
            }
        }
        if (message.getDestination().equals(getLocalID()) || message.getDestination().equals(ID.BROADCASTID)) {
//Debug.println("received a broadcast message");
            if (message.isGeneralMessage()) {
                getNodeInterface().onMessageArrived(message);
            } else if (message.isSTUNMessage()) {
Debug.println("a stun message");
                stunServer.handleSTUNMessage(message);
            } else if (message.isControlMessage()) {
                resolver.onControlMessageArrived(message);
            }
        }
    }

    /** */
    public void onConnectionEstablished(Connection connection, ID id, NodeInfo nodeInfo) {
        if (connection.getConnectionType().equals(Connection.Type.CHILD)) {
Debug.println("connection established!! child " + id);
            childNodes.put(id, connection);
        } else {
Debug.println("connection established!! server " + id);
            serverNodes.put(id, connection);
            serverInfos.put(connection, nodeInfo);
        }
        connection.start();
    }

    /** */
    public void onDisconnect(Connection connection) {
        if (connection.getConnectionType().equals(Connection.Type.CHILD)) {
            childNodes.entrySet().remove(connection);
        } else {
            serverNodes.entrySet().remove(connection);
            serverInfos.remove(connection);
        }
    }

    /** */
    private static Random random = new Random();

    /** */
    public ID getServerIDAtRandom() {
        if (serverNodes.size() == 0) {
            return getLocalID();
        }
        int index = random.nextInt(serverNodes.size());
        return (ID) serverNodes.keySet().toArray()[index];
    }

    /** */
    private void printEntries(Map<ID, Connection> map) {
        for (Object object : map.entrySet()) {
            System.out.println(object);
        }
    }

    /** */
    public void sendMessage(Message message) throws IOException {
Debug.println(message.getMessageContents());
        Connection connection = null;
        if (!message.getDestination().equals(ID.BROADCASTID)) {
Debug.println("check where to send.");
            printEntries(serverNodes);
            if (childNodes.containsKey(message.getDestination())) {
Debug.println("sending a message to a child");
                connection = childNodes.get(message.getDestination());
                connection.write(message.toBytes());
            } else if (serverNodes.containsKey(message.getDestination())) {
Debug.println("sending a message to a server");
                connection = serverNodes.get(message.getDestination());
                connection.write(message.toBytes());
// Debug.println("successful");
            } else {
Debug.println("sending a message to all servers");
                sendToAllServers(message.toBytes());
            }
        } else {
Debug.println("sending broadcast message");
            sendToAllServers(message.toBytes());
            sendToAllChildren(message.toBytes());
        }
    }

    /** */
    public NodeInfo[] getAllNodeInfo() {
        Collection<NodeInfo> values = serverInfos.values();
        return values.toArray(new NodeInfo[values.size()]);
    }

    /** */
    public NodeInfo getLocalNodeInfo() {
        try {
            return new NodeInfo(InetAddress.getLocalHost(), localServer.getLocalPort(), nodeInfoServer.getPort(), this.stunServer.getPort1(), this.stunServer.getPort2());
        } catch (UnknownHostException e) {
            throw (RuntimeException) new IllegalStateException("may not occur").initCause(e);
        }
    }
}

/* */
