package org.onap.so.db.camunda;

public interface CamundaResumeDao {
    String findResumeFromBB(String requestId);
}
