package utils;

public class Attribute {
	private String m_name = null;
	private String m_value = null;
	
	public Attribute() {
		m_name = new String("");
		m_value = new String("");
	}
	
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
	
	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}
	
	public void setValue(int value) {
		m_value = String.valueOf(value);
	}
}
