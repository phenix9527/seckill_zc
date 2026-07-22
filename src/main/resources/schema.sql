
-- 创建秒杀库存表
CREATE TABLE IF NOT EXISTS `seckill` (
    `seckill_id` BIGINT NOT NULL AUTO_INCREMENT comment '商品库存id',
    `name` VARCHAR(128) NOT NULL comment '商品名称',
    `stock` int not null comment '商品库存数量',
    `start_time` DATETIME NOT NULL comment '秒杀开始时间',
    `end_time` DATETIME NOT NULL comment '秒杀结束 时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    PRIMARY KEY (`seckill_id`),
    key idx_start_time(`start_time`),
    key idx_end_time(`end_time`),
    key idx_created_at(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '秒杀库存表';
-- 初始化数据
insert into  seckill (name, stock, start_time, end_time)
values
('1000元秒杀iphone17', 100, '2026-07-21 00:00:00', '2026-07-22 00:00:00'),
('500元秒杀ipad10', 200, '2026-07-21 00:00:00', '2026-07-22 00:00:00'),
('300元秒杀小米8', 300, '2026-07-21 00:00:00', '2026-07-22 00:00:00'),
('200元秒杀红米note', 400, '2026-07-21 00:00:00', '2026-07-22 00:00:00');

-- 秒杀成功明细表
-- 用户登录认证相关信息-简单实现用phone
create table success_killed(
  `seckill_id` BIGINT NOT NULL AUTO_INCREMENT comment '商品商品id',
  `user_phone` BIGINT NOT NULL  comment '用户手机号',
  `state` tinyint not null  default -1 comment  '状态 -1无效 0成功 1已付款 2已发货',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  primary key (seckill_id, user_phone), /* 联合主键 */
  key idx_created_at(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '秒杀成功明细表';
