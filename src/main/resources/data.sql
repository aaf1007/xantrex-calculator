-- Seed intern accounts (password: "test" for all, BCrypt-hashed)
-- Idempotent: ON CONFLICT DO NOTHING prevents duplicates on re-run
INSERT INTO users (email, password, role) VALUES ('test@xantrex.com', '$2b$10$eYbLhTmmAt/drVVghM.iK.rzugvdx6G7hZWajf510SQj23VqkNJlm', 'INTERN') ON CONFLICT (email) DO UPDATE SET role = EXCLUDED.role;
INSERT INTO users (email, password, role) VALUES ('david.miller@xantrex.com', '$2b$10$eYbLhTmmAt/drVVghM.iK.rzugvdx6G7hZWajf510SQj23VqkNJlm', 'INTERN') ON CONFLICT (email) DO UPDATE SET role = EXCLUDED.role;
INSERT INTO users (email, password, role) VALUES ('angela.shi@xantrex.com', '$2b$10$eYbLhTmmAt/drVVghM.iK.rzugvdx6G7hZWajf510SQj23VqkNJlm', 'INTERN') ON CONFLICT (email) DO UPDATE SET role = EXCLUDED.role;
