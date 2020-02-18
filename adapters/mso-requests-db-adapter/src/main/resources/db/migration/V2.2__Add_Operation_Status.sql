use requestdb;

create table if not exists operation_status (
        SERVICE_ID varchar(255) not null,
        OPERATION_ID varchar(255) not null,
        SERVICE_NAME varchar(255),
        OPERATION_TYPE varchar(255),
        USER_ID varchar(255),
        RESULT varchar(255),
        OPERATION_CONTENT varchar(255),
        PROGRESS varchar(255),
        REASON varchar(255),
	ACCESS_SERVICE_ID varchar(255),
        OPERATE_AT datetime,
        FINISHED_AT datetime,
        primary key (SERVICE_ID,OPERATION_ID)
);

create table resource_operation_status (
  SERVICE_ID varchar(255) not null,
  OPERATION_ID varchar(255) not null,
  RESOURCE_TEMPLATE_UUID varchar(255) not null,
  OPER_TYPE varchar(255),
  RESOURCE_INSTANCE_ID varchar(255),
  JOB_ID varchar(255),
  STATUS varchar(255),
  PROGRESS varchar(255),
  ERROR_CODE varchar(255) ,
  STATUS_DESCRIPOTION varchar(255) ,
  primary key (SERVICE_ID,OPERATION_ID,RESOURCE_TEMPLATE_UUID)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
