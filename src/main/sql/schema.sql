--数据库初始化脚本

--创建数据库
CREATE DATABASE seckill;
--使用数据库
USE seckill;
--创建秒杀库存表
--ENGINE=INNODB 支持事务
--AUTO_INCREMENT=1000 从1000开始
CREATE TABLE seckill(
seckill_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
NAME VARCHAR(120) NOT NULL COMMENT '商品名称',
number INT NOT NULL COMMENT '库存数量',
create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
start_time TIMESTAMP NOT NULL COMMENT '秒杀开启时间',
end_time TIMESTAMP NOT NULL COMMENT '秒杀结束时间',
PRIMARY KEY (seckill_id),
KEY idx_start_time(start_time),
KEY idx_end_time(end_time),
KEY idx_create_time(create_time)
)ENGINE=INNODB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='描述库存表';

--要记得把create_time放到另外两个时间前面，不然会报1293错误，因为根据网上的解释，原因是如果你有两个timestamp字段
--但是只把第一个设定为current_timestamp而第二个没有设定默认值，mysql也能成功建表,但是反过来就不行

--初始化数据
INSERT INTO seckill(NAME,number,start_time,end_time)
VALUES
('1000元秒杀iphone6',100,'2016-7-11 00:00:00','2016-7-12 00:00:00'),
('500元秒杀小米4',100,'2016-7-11 00:00:00','2016-7-12 00:00:00'),
('300元秒杀红米note',100,'2016-7-11 00:00:00','2016-7-12 00:00:00'),
('200元秒杀魅蓝',100,'2016-7-11 00:00:00','2016-7-12 00:00:00');

INSERT INTO seckill(NAME,number,start_time,end_time)
VALUES
('10元秒杀球鞋',100,'2016-7-11 00:00:00','2016-7-12 00:00:00');
SHOW CREATE TABLE seckill;
SELECT * FROM seckill;
--秒杀成功明细表
--用户登陆认证相关信息
CREATE TABLE success_killed(
seckill_id BIGINT NOT NULL COMMENT '秒杀商品id',
user_phone BIGINT NOT NULL COMMENT '用户手机号',
state TINYINT NOT NULL DEFAULT -1 COMMENT '状态标示:-1:无效 0：成功 1：已付款 2：已下单',
create_time TIMESTAMP NOT NULL COMMENT '创建时间',
PRIMARY KEY(seckill_id,user_phone),
KEY idx_create_time(create_time)
)ENGINE=INNODB	DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

DESC success_killed;
