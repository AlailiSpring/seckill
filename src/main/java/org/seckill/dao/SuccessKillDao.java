package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;
import org.springframework.stereotype.Repository;

/**
 * Created by Spring on 2016/7/16.
 */
@Repository
public interface SuccessKillDao {

	/**
	 * 插入购买明细，可过滤重复（联合唯一主键）
	 * @param seckillId
	 * @param userPhone
	 * @return
	 */
	int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

	/**
	 * 根据ID查询SuccessKilled,并携带秒杀商品实体
	 * @param seckillId
	 * @return
	 */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}
