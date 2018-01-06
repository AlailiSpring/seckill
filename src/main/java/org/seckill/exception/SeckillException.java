package org.seckill.exception;

/**
 * 秒杀相关异常
 * Created by Spring on 2016/8/24.
 */
public class SeckillException extends RuntimeException {

	public SeckillException(String message) {
		super(message);
	}

	public SeckillException(String message, Throwable cause) {
		super(message, cause);
	}
}
