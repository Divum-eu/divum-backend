CREATE TABLE "default".server_machines
(
    id              UUID PRIMARY KEY,
    ip              VARCHAR(15) NOT NULL UNIQUE,
    total_ram       int         NOT NULL,
    total_cpu_cores INT         NOT NULL,
    free_ram        INT         NOT NULL,
    free_cpu_cores  INT         NOT NULL,
    created_on      DATE        NOT NULL,
    deleted_on      DATE
);

CREATE TABLE IF NOT EXISTS "default".users
(
    id            UUID PRIMARY KEY,
    username      VARCHAR(25)  NOT NULL UNIQUE,
    email_address VARCHAR(254) NOT NULL UNIQUE,
    password_data VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS "default".server_instances
(
    id                UUID PRIMARY KEY,
    owner_id          UUID REFERENCES "default".users (id)           NOT NULL,
    server_machine_id UUID REFERENCES "default".server_machines (id) NOT NULL,
    address           VARCHAR(30)                                    NOT NULL UNIQUE,
    created_on        DATE                                           NOT NULL,
    deleted_on        DATE
);

CREATE TABLE IF NOT EXISTS "default".minecraft_server_instances
(
    id UUID PRIMARY KEY,
    owner_id UUID REFERENCES "default".users(id) NOT NULL,
    server_machine_id UUID REFERENCES "default".server_machines (id) NOT NULL,
    address           VARCHAR(30)                                    NOT NULL UNIQUE,
    created_on        DATE                                           NOT NULL,
    deleted_on        DATE,
    configuration     JSON
)
