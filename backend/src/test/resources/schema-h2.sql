-- H2（MySQL 兼容模式）测试表结构，与 database/init.sql 中的里程碑相关表保持一致
CREATE TABLE baby (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  birthday DATE NOT NULL,
  blood_type VARCHAR(10),
  initial_height DECIMAL(5,2),
  initial_weight DECIMAL(5,2)
);

CREATE TABLE milestone (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  baby_id BIGINT NOT NULL,
  type_code VARCHAR(32) NOT NULL,
  milestone_date DATE NOT NULL,
  note VARCHAR(500),
  photo_url VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_baby_type_date UNIQUE (baby_id, type_code, milestone_date)
);
CREATE INDEX idx_baby_date ON milestone (baby_id, milestone_date);
