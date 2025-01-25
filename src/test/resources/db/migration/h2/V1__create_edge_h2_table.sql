CREATE TABLE IF NOT EXISTS edge
(
    from_id integer,
    to_id   integer
);

INSERT INTO edge (from_id, to_id)
VALUES (1, 2),
       (1, 3),
       (2, 4),
       (2, 5),
       (3, 6);