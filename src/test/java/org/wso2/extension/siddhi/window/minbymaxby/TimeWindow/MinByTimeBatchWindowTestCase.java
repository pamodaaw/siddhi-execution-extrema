package org.wso2.extension.siddhi.window.minbymaxby.TimeWindow;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class MinByTimeBatchWindowTestCase {
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }
    /**
     * Commenting out intermittent failing test case until fix this properly.
     */
    @Test
    public void minbyTimeBatchWindowTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream =
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.minbymaxby:minbytimebatch(price,1 sec) " +
                "select symbol, price " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 888f, 1});
        inputHandler.send(new Object[]{"MIT", 700f, 1});
        Thread.sleep(5000);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 777f, 3});
        inputHandler.send(new Object[]{"WSO2", 234.5f, 4});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 90f, 5});
        inputHandler.send(new Object[]{"WSO2", 765f, 6});
        Thread.sleep(2000);
        Assert.assertEquals(3, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
    @Test
    public void minbyTimeBatchWindowTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream =
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query2') " +
                "from cseEventStream#window.minbymaxby:minbytimebatch(price,1 sec) " +
                "select symbol, price " +
                "insert expired events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 888f, 1});
        inputHandler.send(new Object[]{"MIT", 700f, 1});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 777f, 3});
        inputHandler.send(new Object[]{"WSO2", 234.5f, 4});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 90f, 5});
        inputHandler.send(new Object[]{"WSO2", 765f, 6});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void minbyTimeBatchWindowTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream =
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query2') " +
                "from cseEventStream#window.minbymaxby:minbytimebatch(price,1 sec) " +
                "select symbol, price " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp , inEvents , removeEvents);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 888f, 1});
        inputHandler.send(new Object[]{"MIT", 700f, 1});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 777f, 3});
        inputHandler.send(new Object[]{"WSO2", 234.5f, 4});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 90f, 5});
        inputHandler.send(new Object[]{"WSO2", 765f, 6});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void minbyTimeBatchWindowTest4() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.minbymaxby:minbytimebatch(price,1 sec) join twitterStream#window.minbymaxby:minbytimebatch(company,1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        inEventCount += (inEvents.length);
                    }
                    if (removeEvents != null) {
                        removeEventCount += (removeEvents.length);
                    }
                    eventArrived = true;
                }
            });
            InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
            InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
            executionPlanRuntime.start();
            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
            Thread.sleep(1500);
            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(1000);
            Assert.assertTrue("In Events can be 1 or 2 ", inEventCount == 1 || inEventCount == 2);
            Assert.assertEquals(0, removeEventCount);
            Assert.assertTrue(eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }
}

