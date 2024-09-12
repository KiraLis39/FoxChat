package door.Message;

import java.text.SimpleDateFormat;
import com.google.gson.Gson;


public class MessageDTO {
	public enum GlobalMessageType {
		NULL_MESSAGE,
		
		PUBLIC_MESSAGE,	
		PRIVATE_MESSAGE, 
		SYSINFO_MESSAGE,	
		ERROR_MESSAGE,	
		
		AUTH_REQUEST,
		CONFIRM_AUTH_MESSAGE, 
		REJECT_AUTH_MESSAGE,
		
		PASS_REQUEST,
		CONFIRM_PASS_MESSAGE,	
		REJECT_PASS_MESSAGE,
		
		USERLIST_MESSAGE
	}
	private GlobalMessageType messageType;	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("(dd.MM.yyyy) HH:mm:ss");
	
	private String body, to, from;	
	private String uid, password;
	private long timestamp;

	
	public MessageDTO() {this(GlobalMessageType.NULL_MESSAGE, null, null);}
	
	public MessageDTO(GlobalMessageType type, String from, String to) {this(type, from, to, null);}
	
	public MessageDTO(GlobalMessageType type, String from, String to, String body) {this(type, from, to, body, -1);}
	
	public MessageDTO(GlobalMessageType type, String from, String to, String body, long timestamp) {this(type, from, to, body, null, null, timestamp);}
	
	public MessageDTO(GlobalMessageType type, String from, String to, String body, String uid, String password, long timestamp) {
		this.messageType = type;
		this.uid = uid;
		this.from = from;
		this.to = to;
		this.body = body;
		this.password = password;
		this.timestamp = timestamp == -1 ? System.currentTimeMillis() : timestamp;
	}
	
	public String convertToJson() {return new Gson().toJson(this);}
	
	public static MessageDTO convertFromJson(String jsonString) {
		return new Gson().fromJson(jsonString, MessageDTO.class);
	}
	
	
	public GlobalMessageType getMessageType() {return this.messageType;}
	public void setMessageType(GlobalMessageType messageType) {this.messageType = messageType;}
	
	public String getPassword() {return this.password;}
	public void setPassword(String password) {this.password = password;}
	
	public String getFrom() {return this.from;}
	public void setFrom(String from) {this.from = from;}
	
	public String getBody() {return this.body;}
	public void setBody(String body) {this.body = body;}
	
	public String getTo() {return this.to;}
	public void setTo(String to) {this.to = to;}
	
	public long getTimestamp() {return this.timestamp;}
	public void setTimestamp(long timestamp) {this.timestamp = timestamp;}	
		
	public String getUid() {return this.uid;}
	public void setUid(String _uid) {this.uid = _uid;}
	

	@Override
	public String toString() {
        return "[" + this.messageType + "] FROM '" + this.from + "' TO '" + this.to + "': '" + this.body + "' (" + dateFormat.format(this.timestamp) + ");";
    }
}