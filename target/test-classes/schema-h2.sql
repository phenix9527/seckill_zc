-- H2 兼容建表脚本（测试环境，去掉 MySQL 专有语法）

-- 秒杀库存表
CREATE TABLE IF NOT EXISTS seckill (
    seckill_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    stock INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (seckill_id)
);

-- 初始化数据
INSERT INTO seckill (name, stock, start_time, end_time) VALUES
('1000元秒杀iphone17', 100, '2026-07-21 00:00:00', '2026-07-22 00:00:00'),
('500元秒杀ipad10', 200, '2026-07-21 00:00:00', '2026-07-22 00:00:00'),
('300元秒杀小米8', 300, '2026-07-21 00:00:00', '2026-07-22 00:00:00'),
('200元秒杀红米note', 400, '2026-07-21 00:00:00', '2026-07-22 00:00:00');

-- 秒杀成功明细表（联合主键 seckill_id + user_phone）
CREATE TABLE IF NOT EXISTS success_killed (
    seckill_id BIGINT NOT NULL,
    user_phone BIGINT NOT NULL,
    state TINYINT NOT NULL DEFAULT -1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (seckill_id, user_phone)
);
