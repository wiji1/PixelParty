package dev.wiji.pixelparty.sql;
public class QueryStorage {
	public String fieldName;
	public Object value;

	public QueryStorage(String fieldName, Object value) {
		this.fieldName = fieldName;
		this.value = value;
	}
}
