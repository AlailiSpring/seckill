package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 * 业务接口：站在"使用者"的角度设计接口
 * 三个方面：方法定义粒度，参数，返回类型（友好的返回类型/异常）
 * Created by Spring on 2016/8/24.
 */
public interface SeckillService  {

	/**
	 * 查询所有秒杀记录
	 * @return
	 */
	List<Seckill> getSeckillList();

	/**
	 * 查询单个秒杀记录
	 * @param seckillId
	 * @return
	 */
	Seckill getById(long seckillId);

	/**
	 * 当秒杀快开始的时候，输出秒杀接口地址，否则输出系统时间和秒杀时间
	 * 防止用户使用插件或者经验得到我们的秒杀地址，参与恶性竞争
	 * @param seckillId
	 */
	Exposer exportSeckillUrl(long seckillId);

	/**
	 * 执行秒杀操作，需要与内部系统的md5进行对比，当md5发生了改变了之后，拒绝执行秒杀
	 * 异常抛出多种是为了可以告诉用户到底是哪个异常发生了
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 */
	SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException,RepeatKillException,SeckillCloseException;
}
