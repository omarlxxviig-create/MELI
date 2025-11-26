ALTER TABLE users ADD COLUMN google_sub VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN picture_url VARCHAR(500);
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_users_google_sub ON users(google_sub);
