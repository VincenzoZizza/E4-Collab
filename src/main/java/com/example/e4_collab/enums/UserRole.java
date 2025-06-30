package com.example.e4_collab.enums;

public enum UserRole {
    ROLE_ADMIN(0),
    ROLE_EDITOR(1),
    ROLE_USER(2);
	
	Integer value;

	private UserRole(Integer value) {
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}
    
	public static UserRole fromValue(Integer value) {
	    for (UserRole role : UserRole.values()) {
	        if (role.getValue().equals(value)) {
	            return role;
	        }
	    }
	    throw new IllegalArgumentException("Valore non valido: " + value);
	}
    
}
