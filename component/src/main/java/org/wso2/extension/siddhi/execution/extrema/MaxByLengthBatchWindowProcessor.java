/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.extension.siddhi.execution.extrema;

import org.wso2.extension.siddhi.execution.extrema.util.Constants;

/**
 * This class is a window processor maxByLengthBatch extension
 * <p>
 * Sample Query:
 * <p>
 * from inputStream#window.minbymaxby:maxByLengthBatch(attribute1, 4)
 * select attribute1, attribute2
 * insert into outputStream;
 * <p>
 * Description:
 * In the example query given, 4 is the number of events the window should tumble and attribute1 is the attribute
 * that need to be compared to find the event which has max value.
 * According to the given attribute it will give event which holds the maximum value.
 */
public class MaxByLengthBatchWindowProcessor extends MaxByMinByLengthBatchWindowProcessor {

    public MaxByLengthBatchWindowProcessor() {
        super.minByMaxByExecutorType = Constants.MAX_BY;
    }

}
