package org.seckill.exception;

/**
 * 秒杀关闭异常
 * Created by Spring on 2016/8/24.
 */
public class SeckillCloseException extends SeckillException {

	public SeckillCloseException(String message) {
		super(message);
	}

	public SeckillCloseException(String message, Throwable cause) {
		super(message, cause);
	}
}
