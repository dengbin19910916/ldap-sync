create table data_source
(
  id        int auto_increment
    primary key,
  name      varchar(255) null,
  url       varchar(255) null,
  username  varchar(255) null,
  password  varchar(255) null,
  pull_strategy int          null
);

create table dictionary
(
  id             int auto_increment
    primary key,
  data_source_id int           null,
  type           int           null,
  base           varchar(2000) null,
  filter         varchar(2000) null,
  constraint FK_dictionary_data_source
    foreign key (data_source_id) references data_source (id)
);

create table attribute_map
(
  id            int auto_increment
    primary key,
  dictionary_id int          null,
  source_name   varchar(255) null,
  target_name   varchar(255) null,
  pattern       varchar(255) null,
  constraint FK_attribute_map_dictionary
    foreign key (dictionary_id) references dictionary (id)
);

create table department
(
  id                varchar(36)   not null
    primary key,
  id_path           varchar(255)  null,
  number            varchar(255)  null,
  number_path       varchar(255)  null,
  name              varchar(255)  null,
  name_path         varchar(2000) null,
  english_name      varchar(255)  null,
  english_name_path varchar(2000) null,
  email             varchar(255)  null,
  organization_type varchar(255)  null,
  parent_name       varchar(255)  null,
  parent_number     varchar(255)  null,
  person_in_charge  varchar(255)  null,
  sequence          int           null,
  data_source_id    int           null,
  constraint FK_department_data_source
    foreign key (data_source_id) references data_source (id)
);

create table employee
(
  id                      varchar(36)  not null
    primary key,
  uid                     varchar(255) null,
  password                tinyblob     null,
  number                  varchar(255) null,
  name                    varchar(255) null,
  pinyin                  varchar(255) null,
  address                 varchar(255) null,
  birthday                datetime     null,
  company_name            varchar(255) null,
  company_number          varchar(255) null,
  department_number       varchar(255) null,
  email                   varchar(255) null,
  english_name            varchar(255) null,
  gender                  varchar(255) null,
  mobile                  varchar(255) null,
  mobile_short            varchar(255) null,
  nation                  varchar(255) null,
  part_time_department_id varchar(255) null,
  part_time_position      varchar(255) null,
  position_number         varchar(255) null,
  position_name           varchar(255) null,
  status                  varchar(255) null,
  telephone_number        varchar(255) null,
  telephone_short         varchar(255) null,
  level                   varchar(255) null,
  sequence                int          null,
  department_id           varchar(36)  null,
  data_source_id          int          null,
  constraint FK_employee_department
    foreign key (department_id) references department (id),
  constraint FK_employee_data_source
    foreign key (data_source_id) references data_source (id)
);
