-- 7.8.0 to 7.8.2 upgrade

USE `camundabpmn`;
-- https://app.camunda.com/jira/browse/CAM-8485
drop index ACT_IDX_HI_ACT_INST_STATS on ACT_HI_ACTINST;
create index ACT_IDX_HI_ACT_INST_STATS on ACT_HI_ACTINST(PROC_DEF_ID_, PROC_INST_ID_, ACT_ID_, END_TIME_, ACT_INST_STATE_);
create index ACT_IDX_HI_PRO_INST_PROC_TIME on ACT_HI_PROCINST(START_TIME_, END_TIME_);
