package cn.com.lcase.app.net;

import java.io.Serializable;

public class ReturnVo<T>  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7283825109842043730L;
	private boolean success = false;
	private String message;
	
	private T data;
	
	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}
	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(T data) {
		this.data = data;
	}
	
	public static ReturnVo<Object> GetVo(){
		return new ReturnVo<Object>();
	}
	
	
}
