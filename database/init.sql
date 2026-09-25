CREATE TABLE IF NOT EXISTS baby (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  birthday DATE NOT NULL,
  blood_type VARCHAR(10),
  initial_height DECIMAL(5,2),
  initial_weight DECIMAL(5,2)
);

CREATE TABLE IF NOT EXISTS growth_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  baby_id BIGINT NOT NULL,
  recorded_at DATE NOT NULL,
  height_cm DECIMAL(5,2),
  weight_kg DECIMAL(5,2),
  percentile VARCHAR(40)
);

CREATE TABLE IF NOT EXISTS vaccine_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  baby_id BIGINT NOT NULL,
  vaccine_name VARCHAR(120) NOT NULL,
  planned_date DATE NOT NULL,
  completed BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS food_recipe (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  month_age_min INT NOT NULL,
  month_age_max INT NOT NULL,
  name VARCHAR(120) NOT NULL,
  ingredients TEXT,
  steps TEXT,
  nutrition TEXT,
  allergens VARCHAR(160)
);

-- 成长里程碑时间线
CREATE TABLE IF NOT EXISTS milestone (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  baby_id BIGINT NOT NULL,
  type_code VARCHAR(32) NOT NULL COMMENT '里程碑类型，见 MilestoneType 枚举',
  milestone_date DATE NOT NULL COMMENT '实际发生日期',
  note VARCHAR(500) COMMENT '说明',
  photo_url VARCHAR(500) COMMENT '已有图片地址',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间，用于判定同日同类型最早一条',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_baby_type_date (baby_id, type_code, milestone_date),
  KEY idx_baby_date (baby_id, milestone_date)
);

-- 演示数据：日期相对今天动态生成，保证始终落在“出生日~今天”范围内
INSERT IGNORE INTO baby (id, name, birthday, blood_type, initial_height, initial_weight)
SELECT 1, '小满', DATE_SUB(CURDATE(), INTERVAL 320 DAY), 'A型', 50.00, 3.30
WHERE NOT EXISTS (SELECT 1 FROM baby WHERE id = 1);

INSERT IGNORE INTO milestone (id, baby_id, type_code, milestone_date, note, photo_url)
SELECT 1, 1, 'turn_over', DATE_SUB(CURDATE(), INTERVAL 200 DAY), '睡着睡着自己翻过去了，吓了一跳', NULL
WHERE NOT EXISTS (SELECT 1 FROM milestone WHERE id = 1);

INSERT IGNORE INTO milestone (id, baby_id, type_code, milestone_date, note, photo_url)
SELECT 2, 1, 'tooth', DATE_SUB(CURDATE(), INTERVAL 90 DAY), '下门牙冒出小白点，口水变多了', NULL
WHERE NOT EXISTS (SELECT 1 FROM milestone WHERE id = 2);
