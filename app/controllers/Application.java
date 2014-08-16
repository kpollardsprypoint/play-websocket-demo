package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.libs.F;

import play.mvc.Controller;
import views.html.*;

import java.util.HashMap;
import java.util.Map;

public class Application extends Controller {

    private static Integer socketNumber = 0;
    private static HashMap<Integer, WebSocket.Out> outSockets = new HashMap<>();

    public static Result index() {
        return ok(index.render("WebSocket Test..."));
    }

    public static WebSocket<String> getNotifier() {
        return new WebSocket<String>() {

            private Integer mySocketNumber;

            // Called when the WebSocket Handshake is done.
            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> out) {

                // For each event received on the socket,
                in.onMessage(new F.Callback<String>() {
                    public void invoke(String event) {

                        // Log events to the console
                        System.out.println(event);

                        out.write("Confirmed... [" + event + "]");

                        broadcastMessage(mySocketNumber, "Socket [" + mySocketNumber + "] has contributed this pearl of wisdom: ["+ event +"]");

                    }
                });

                // When the socket is closed.
                in.onClose(new F.Callback0() {
                    public void invoke() {

                        System.out.println("Closing socket " + mySocketNumber);
                        outSockets.remove(mySocketNumber);

                        broadcastMessage(mySocketNumber, "Socket " + mySocketNumber + " has left the building");

                    }
                });


                // Make sure we remember the socket
                socketNumber++;
                mySocketNumber = socketNumber;
                outSockets.put(socketNumber, out);

                // Send a single 'Hello!' message
                out.write("Hello. We are live with socket " + mySocketNumber + "!");

                broadcastMessage(mySocketNumber, "Socket " + mySocketNumber + " has joined the party.");

            }

        };
    }

    private static void broadcastMessage(Integer broadcaster, String message){
        System.out.println("About to broadcast to " + outSockets.size() + " sockets.");

        for (Map.Entry<Integer, WebSocket.Out> socket : outSockets.entrySet()) {
            // No need to broadcast to the broadcaster.
            if (socket.getKey() != broadcaster)
                socket.getValue().write(message);
        }
    }

}
