create schema if not exists divum;

create table if not exists divum.server_machines
(
    id              uuid default uuidv4()       not null
        primary key,
    created_on      timestamp(6) with time zone not null,
    deleted_on      timestamp(6) with time zone,
    free_cpu_cores  float not null,
    free_ram_mb        integer not null,
    ip              varchar(15)                 not null
        constraint uk_server_machines_ip
            unique,
    total_cpu_cores float not null,
    total_ram_mb       integer not null
);

create table if not exists divum.users
(
    id            uuid default uuidv4()       not null
        primary key,
    created_on    timestamp(6) with time zone not null,
    deleted_on    timestamp(6) with time zone,
    email_address varchar(254)                not null,
    password_data varchar(255)                not null,
    username      varchar(30)                 not null,
    constraint uk_users_username_email_address
        unique (username, email_address)
);

create table if not exists divum.server_instances
(
    id                uuid default uuidv4()       not null
        primary key,
    address           varchar(50)                 not null,
    created_on        timestamp(6) with time zone not null,
    deleted_on        timestamp(6) with time zone,
    name              varchar(50)                 not null,
    owner_id          uuid                        not null
        constraint fk_server_instances_users
            references divum.users,
    server_machine_id uuid                        not null
        constraint fk_server_instances_server_machines
            references divum.server_machines
);

create table if not exists divum.minecraft_server_instances
(
    configuration json not null,
    daemon_id varchar(36) not null,
    id            uuid not null
        primary key
        constraint fk_minecraft_server_instances_server_instances
            references divum.server_instances
);