package de.uniba.wiai.dsg.pks.assignment1.histogram.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelBlockingQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OutputServiceThreadWrapper extends Thread{
    private final static int MESSAGE_CAPACITY = 100;
    private final OutputService out;
    private final BlockingQueue<Message> queue;

    public OutputServiceThreadWrapper(Service type){
        super("OutputService");
        this.out = new OutputService();
        if(Service.HIGH_LEVEL.equals(type)){
            this.queue = new ArrayBlockingQueue<>(MESSAGE_CAPACITY);
        } else {
            this.queue = new LowLevelBlockingQueue<>(MESSAGE_CAPACITY);
        }
    }

    @Override
    public void run(){
        printIncomingMessages();
    }

    private void printIncomingMessages() {
        boolean finished = false;
        while(!finished){
            try {
                Message message = queue.take();
                if (MessageType.FINISH.equals(message.getType())) {
                    finished = true;
                    continue;
                }
                if(MessageType.FIlE.equals(message.getType())){
                    out.logProcessedFile(message.getPath());
                } else{
                    out.logProcessedDirectory(message.getPath(), message.getHistogram());
                }
            } catch (InterruptedException exception) {
                finished = true;
            }
        }
    }

    public void put(Message message) throws InterruptedException {
        queue.put(message);
    }

}
