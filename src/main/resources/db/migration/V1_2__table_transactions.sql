CREATE TABLE transactions (
  id                INTEGER PRIMARY KEY,
  block_hash        VARCHAR REFERENCES blocks (hash),
  hash              VARCHAR NOT NULL UNIQUE,
  amount            INTEGER NOT NULL,
  timestamp         BIGINT  NOT NULL,
  recipient_key     VARCHAR NOT NULL,
  sender_key        VARCHAR NOT NULL,
  signature         VARCHAR NOT NULL,
  sender_address    VARCHAR NOT NULL,
  recipient_address VARCHAR NOT NULL
);