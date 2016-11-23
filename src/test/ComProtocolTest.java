package test;

import com.protocol.Protocol;

public class ComProtocolTest {
	
	public static void main(String[] args){
		char[] msg = {254,205,0,5,5,7,8,9,10,253,168,};
		Protocol prot = new Protocol();
//		for(char b: msg)
//			if(prot.parseChar(b)){
//				System.out.println("Payload:");
//				for(byte p: prot.getPayload()){
//					System.out.print((p&0x0FF) + ", ");;
//				}
//			}
		byte[] pay = {0,(byte)0x80,(byte) 0xbb,0x44};
		for(byte b: prot.pack(pay))
			System.out.print((b&0x0FF)+",");
	}

}
