package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKillDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by Spring on 2016/9/1.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//注入service依赖
	//spring在找不到匹配 Bean 时也不报错
	@Autowired
	private SeckillDao seckillDao;


	//@Qualifier("successKillDao"),和Autowired一起搭配使用,会使自动注入的策略从byType变成byName
	//@Autowired 可以对成员变量、方法以及构造函数进行注释,而 @Qualifier 的标注对象是成员变量、方法入参、构造函数入参
	//常用于解决的问题：我们在 Spring 容器中配置了两个类型为 Office 类型的 Bean，当对Boss的 office 成员变量进行自动注入时，
	// Spring 容器将无法确定到底要用哪一个 Bean，因此异常发生了
	@Autowired
	private SuccessKillDao successKillDao;

	//用于混淆的言值,越复杂越好
	private final String salt = "safdsfds55466547ghjh78ikjjk;.b";

	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 10);
	}

	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	public Exposer exportSeckillUrl(long seckillId) {
		//优化点：需要进行优化的地方，可以使用redis进行缓存，降低数据库访问的压力
		/**
		 * 伪代码逻辑：
		 * get from cache
		 * if null
		 *    get db
		 * else
		 *    put cache
		 * login
		 */
		Seckill seckill = seckillDao.queryById(seckillId);
		if (seckill == null) {
			return new Exposer(false, seckillId);
		}
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();

		//当前系统时间
		Date nowTime = new Date();

		if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {

			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}

		//md5转化为字符串的过程，不可逆，加入混淆的东西
		String md5 = getMD5(seckillId);
		return new Exposer(true, md5, seckillId);
	}

	//获取MD5码
	private String getMD5(long seckillId) {
		String base = seckillId + "/" + salt;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

	@Transactional
	/*
	加上了Transactional注解的方法需要尽量的单纯保证对数据库的操作，不要穿插的执行其余的网络操作！
	使用注解控制事物方法的优点：
	1、开发团队达成一致约定，明确标注事物方法的编码风格
	2、保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，RPC/HTTP请求/Redis操作之类的方法剥离到事务方法外部（可以在外部在封装一层，把这些操作剥离出
	事务操作之外，尽量保证事务操作的干净）
	3、不是所有的方法都需要事务，如只有一条修改语句，只读操作不需要事务控制，可以了解一下MySql的行级索相关的概念
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
		if (md5 == null || !md5.equals(getMD5(seckillId).trim())) {
			throw new SeckillException("seckill Data rewrite");
		}
		//执行秒杀逻辑：减库存+记录购买行为
		Date nowTime = new Date();

		//执行过程中出现的任何异常都应该抛出来，例如数据库连接断了，执行插入的时候出现了异常
		//发生异常时候需要回滚，防止因为多去减库存这种事情的发生
		try {
			//减库存
			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
			if (updateCount < 0) {
				//没有秒杀记录，秒杀结束
				throw new SeckillCloseException("seckill is closed!");
			} else {
				//记录购买行为
				int insertCount = successKillDao.insertSuccessKilled(seckillId, userPhone);
				//唯一性验证在于seckillId和userPhone联合主键，使用insert ignore来判断
				if (insertCount <= 0) {
					throw new RepeatKillException("Repeat seckill");
				} else {
					//秒杀成功
					SuccessKilled successKilled = successKillDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
				}
			}
		} catch (SeckillCloseException e1) {
			logger.error(e1.getMessage(), e1);
			throw new SeckillCloseException("Seckill is over!error message is " + e1.getMessage());
		} catch (RepeatKillException e2) {
			logger.error(e2.getMessage(), e2);
			throw new RepeatKillException("Repeat kill!error message is " + e2.getMessage());
		} catch (SeckillException e) {
			//整体用异常括起来，把检查型（编译期）异常转化为运行期异常
			logger.error(e.getMessage(), e);
			throw new SeckillException("Seckill inner error:" + e.getMessage());
		}

	}
}
