package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.OutputService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelBlockingQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OutputServiceThread extends Thread{
    private final static int MESSAGE_CAPACITY = 100;
    private final OutputService outputService;
    private final BlockingQueue<Message> queue;

    public OutputServiceThread(Service type){
        super("OutputService");
        this.outputService = new OutputService();
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
                if(MessageType.FILE.equals(message.getType())){
                    outputService.logProcessedFile(message.getPath());
                } else{
                    outputService.logProcessedDirectory(message.getPath(), message.getHistogram());
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
