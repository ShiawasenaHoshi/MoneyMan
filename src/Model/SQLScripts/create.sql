CREATE TABLE IF NOT EXISTS USERS
(
    NAME        TEXT    PRIMARY KEY NOT NULL,
    PASSWORD    CHAR(64)
);

CREATE TABLE IF NOT EXISTS ACCOUNTS
(
    ID          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    DESCR       TEXT                NOT NULL,
    USER_NAME   TEXT                NOT NULL
);

CREATE TABLE IF NOT EXISTS RECORDS
(
    ID          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    DESCR       TEXT                NOT NULL,
    AMOUNT      REAL                NOT NULL,
    CREATE_TIME BIGINT              NOT NULL,
    CATEGORY_NAME TEXT              NOT NULL,
    ACCOUNT_ID  INT                 NOT NULL
);

CREATE TABLE IF NOT EXISTS CATEGORIES
(
    NAME        TEXT PRIMARY KEY    NOT NULL,
    DESCR       TEXT                NOT NULL
);