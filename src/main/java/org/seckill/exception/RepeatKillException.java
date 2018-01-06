package org.seckill.exception;

/**
 * 重复秒杀异常（运行期异常）
 * 运行期异常不需要我们去手动的try catch
 * Spring 声明式事务只接收运行期异常，别的异常是不会回滚的
 * Created by Spring on 2016/8/24.
 */
public class RepeatKillException extends SeckillException {

	public RepeatKillException(String message) {
		super(message);
	}

	public RepeatKillException(String message, Throwable cause) {
		super(message, cause);
	}
}
