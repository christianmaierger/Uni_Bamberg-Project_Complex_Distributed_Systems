package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.FileMessage;

import java.util.List;

public class LoadBalancer extends AbstractActor {
    private final Router router;

    public LoadBalancer(List<Routee> routees) {
        //TODO: Think about routing strategies and read up on them
        router = new Router(new SmallestMailboxRoutingLogic(), routees);
    }

    public static Props props(List<Routee> routees){
        return Props.create(LoadBalancer.class, () -> new LoadBalancer(routees));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FileMessage.class, message -> router.route(message, getSender())).build();
    }


}
