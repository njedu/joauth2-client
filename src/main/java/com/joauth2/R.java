package com.joauth2;

/**
 * 默认成功：code-10000,msg-操作成功
 * @author Administrator
 *
 */
public class R {

	/**
	 * 10000-成功
	 * @return
	 */
	public static R ok(){
		return new R();
	}

	public static R ok(String msg, Object o){
		return new R(10000, msg, o);
	}

	/**
	 * 10000-成功
	 * @param obj
	 * @return
	 */
	public static R ok(Object obj){
		return new R(obj);
	}

	/**
	 * 失败：10021-网络连接异常
	 * @return
	 */
	public static R error(){
		return new R(10021, "操作失败");
	}

	/**
	 * 失败：10001-自定义messaage
	 * @param message
	 * @return
	 */
	public static R error(String message){
		return new R(10001, message);
	}

	/**
	 * 失败，自定义code和message
	 * @param code
	 * @param message
	 * @return
	 */
	public static R error(int code, String message){
		return new R(code, message);
	}

	/**
	 * 成功
	 */
	public R() {
		this.code=10000;
		this.msg="操作成功";
	}

	/**
	 * 通用函数：
	 * 	-不携带object参数
	 * @param code
	 * @param msg
	 */
	public R(Integer code, String msg) {
		this.code=code;
		this.msg=msg;
	}

	/**
	 * 通用函数
	 * @param code
	 * @param msg
	 * @param object
	 */
	public R(Integer code, String msg, Object object) {
		this.code=code;
		this.msg=msg;
		this.object=object;
	}

	/**
	 * 成功：
	 * 	-携带object
	 * @param object
	 */
	public R(Object object) {
		this.code=10000;
		this.msg="操作成功";
		this.object=object;
	}

	private Integer code;
	private String msg;
	private Object object;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}


}
