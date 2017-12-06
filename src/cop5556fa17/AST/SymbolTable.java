package cop5556fa17.AST;

import java.util.HashMap;

public class SymbolTable {
	
	HashMap<String, Declaration> hm;
	
	public SymbolTable(){
		hm = new HashMap<>();
	}
	
	public Declaration lookup(String str) {
		if(hm.containsKey(str)) {
			Object temp = hm.get(str);
			return (Declaration)temp;
		}
		else
			return null;
	}
	
	public boolean insert(String str, Declaration dec) {
		if(!(hm.containsKey(str))) {
			hm.put(str, dec);
			return true;
		}
		return false;
	}
	


}
