
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.window.minbymaxby;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
//import org.wso2.siddhi.core.util.parser.CollectionOperatorParser;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mathuriga on 29/09/16.
 */
public class MaxByMinByLengthBatchWindowProcessor extends WindowProcessor implements FindableProcessor {
    private int length;
    private int count = 0;
    protected String functionType;
    private ExpressionExecutor functionParameter;
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ExecutionPlanContext executionPlanContext;
    private VariableExpressionExecutor[] variableExpressionExecutors;
    private MaxByMinByExecutor maxByMinByExecutor;
    private StreamEvent oldEvent;
    private StreamEvent resultEvent;
    ComplexEventChunk<StreamEvent> resultStreamEventChunk = new ComplexEventChunk<StreamEvent>(true);
    private StreamEvent toExpireEvent;

    public MaxByMinByLengthBatchWindowProcessor() {

    }


    @Override
    protected void init(ExpressionExecutor[] expressionExecutors, ExecutionPlanContext executionPlanContext) {
        maxByMinByExecutor = new MaxByMinByExecutor();
        this.executionPlanContext = executionPlanContext;
        if (functionType == "MIN") {
            maxByMinByExecutor.setFunctionType(functionType);
            variableExpressionExecutors = new VariableExpressionExecutor[attributeExpressionExecutors.length - 1];
            if (attributeExpressionExecutors.length == 2) {
                variableExpressionExecutors[0] = (VariableExpressionExecutor) attributeExpressionExecutors[0];
                functionParameter = variableExpressionExecutors[0];
                length = (Integer) (((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue());
            }
        } else {
            maxByMinByExecutor.setFunctionType(functionType);
            variableExpressionExecutors = new VariableExpressionExecutor[attributeExpressionExecutors.length - 1];
            if (attributeExpressionExecutors.length == 2) {
                variableExpressionExecutors[0] = (VariableExpressionExecutor) attributeExpressionExecutors[0];
                functionParameter = variableExpressionExecutors[0];
                length = (Integer) (((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue());
            }

        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        List<ComplexEventChunk<StreamEvent>> streamEventChunks = new ArrayList<ComplexEventChunk<StreamEvent>>();
        synchronized (this) {
            ComplexEventChunk<StreamEvent> outputStreamEventChunk = new ComplexEventChunk<StreamEvent>(true);
            long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                StreamEvent currentEvent = streamEventCloner.copyStreamEvent(streamEvent);

                if (count == 0) {
                    outputStreamEventChunk.clear();
                    resultStreamEventChunk.clear();
                    oldEvent=null;
                    //clonedResultEvent=resultEvent;
                }

                if(functionType=="MAX") {
                    resultEvent = maxByMinByExecutor.getMaxEventBatchProcessor(currentEvent, oldEvent, functionParameter);
                    oldEvent=resultEvent;
                }
                else if (functionType=="MIN"){
                    resultEvent=maxByMinByExecutor.getMinEventBatchProcessor(currentEvent,oldEvent,functionParameter);
                    oldEvent=resultEvent;
                }

                count++;
                if (count == length) {
                    //get the results
                     if(resultEvent!=null){
                         if(toExpireEvent!=null) {
                             outputStreamEventChunk.add(toExpireEvent);
                             //resultStreamEventChunk.add(resultEvent);
                         }
                         outputStreamEventChunk.add(resultEvent);
                         toExpireEvent=streamEventCloner.copyStreamEvent(resultEvent);
                         toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                         System.out.println(outputStreamEventChunk);
                     }
                    count = 0;
                    if (outputStreamEventChunk.getFirst() != null) {
                        streamEventChunks.add(outputStreamEventChunk);
                    }
                }

            }
        }
        for (ComplexEventChunk<StreamEvent> outputStreamEventChunk : streamEventChunks) {
            nextProcessor.process(outputStreamEventChunk);
        }


    }


    @Override
    public void start() {
        //do nothing
    }

    @Override
    public void stop() {
        //do nothing
    }

    @Override
    public Object[] currentState() {
        return null;
    }

    @Override
    public void restoreState(Object[] state) {
//
    }

    /**
     * To find the parameter value of given parameter for each event .
     *
     * @param functionParameter name of the parameter of the event data
     * @param streamEvent       event  at processor
     * @return the parameterValue
     */
    public Object getParameterValue(ExpressionExecutor functionParameter, StreamEvent streamEvent) {
        Object parameterValue;
        parameterValue = functionParameter.execute(streamEvent);
        return parameterValue;
    }

    @Override
    public StreamEvent find(StateEvent stateEvent, Finder finder) {
        return finder.find(stateEvent, toExpireEvent, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> list, Map<String, EventTable> map) {
        return OperatorParser.constructOperator(resultStreamEventChunk, expression, matchingMetaStateHolder,executionPlanContext,list,map);
    }
}
