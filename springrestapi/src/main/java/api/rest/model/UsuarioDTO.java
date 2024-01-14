package api.rest.model;

import java.io.Serializable;

public class UsuarioDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String userLogin;
    private String userNome;
    private String usercpf;
	
    public UsuarioDTO(Usuario usuario) {
		
    	this.userLogin = usuario.getLogin();
    	this.userNome = usuario.getNome();
    	this.usercpf = usuario.getCpf();
    	
	}
    
    
    
    public String getUserLogin() {
		return userLogin;
	}
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}
	public String getUserNome() {
		return userNome;
	}
	public void setUserNome(String userNome) {
		this.userNome = userNome;
	}
	public String getUsercpf() {
		return usercpf;
	}
	public void setUsercpf(String usercpf) {
		this.usercpf = usercpf;
	}
	
	
	
	
	
}
