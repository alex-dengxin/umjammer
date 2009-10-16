CREATE DATABASE yamatonadeshiko;

GRANT ALL PRIVILEGES ON yamatonadeshiko.*
TO yamatonadeshiko@localhost
IDENTIFIED BY 'yamatonadeshiko' WITH GRANT OPTION;

USE yamatonadeshiko;

/**
 * Main data base
 *
 * @field type
 *  0: undefined
 *  1: female manager
 *  2: female
 *  3: male manager
 *  4: male
 */
DROP TABLE IF EXISTS Mail;
CREATE TABLE Mail (
  email VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  type TINYINT UNSIGNED NOT NULL,
  unit VARCHAR(255),
  comment TEXT,
  updateTime TIMESTAMP,

  PRIMARY KEY(email, unit)
);
