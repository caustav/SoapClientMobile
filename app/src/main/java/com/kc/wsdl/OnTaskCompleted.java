package com.kc.wsdl;

import com.kc.wsdl.model.TaskCompleteResponse;

/**
 * Created by kc on 10/7/16.
 */
public interface OnTaskCompleted {

    void OnTaskCompleted(TaskCompleteResponse taskCompleteResponse);
}
