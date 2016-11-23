package com.protocol;

public class Protocol {
	
	private static enum PARSE_STATUS{
		IDDLE,
		GOT_HEADER1,
		GOT_HEADER2,
		GOT_LENGTH1,
		GOT_LENGTH2,
		GOT_PAYLOAD,
		GOT_CRC1;
	}
	
	private static final byte HEADER1 		= (byte)0xFE;
	private static final byte HEADER2 		= (byte)0xCD;
	private static final int  MIN_MSG_LEN	= 6;
	
	private PARSE_STATUS parseStat = PARSE_STATUS.IDDLE;
	private int lenght;
	private int index;
	private byte[] payload;
	private char crc1;
	private char crc2;
	private char crc;
	
	private byte[] lastPayload;
	
	public boolean parseChar(char data){
		switch (parseStat) {
		case IDDLE:
			lenght = 0;
			index = 0;
			if(data == 0x0FE)
				parseStat = PARSE_STATUS.GOT_HEADER1;
			break;
		case GOT_HEADER1:
			if(data == 0x0CD)
				parseStat = PARSE_STATUS.GOT_HEADER2;
			break;
		case GOT_HEADER2:
			this.lenght = data<<8;
			parseStat = PARSE_STATUS.GOT_LENGTH1;
			break;
		case GOT_LENGTH1:
			this.lenght += data;
			payload = new byte[lenght];
			if(lenght == 0)
				parseStat = PARSE_STATUS.GOT_PAYLOAD;
			else
				parseStat = PARSE_STATUS.GOT_LENGTH2;
			break;
		case GOT_LENGTH2:
			payload[index++] = (byte) data;
			if(index == lenght)
				parseStat = PARSE_STATUS.GOT_PAYLOAD;
			break;
		case GOT_PAYLOAD:
			this.crc1 = (char) (data & 0x0FF);
			this.parseStat = PARSE_STATUS.GOT_CRC1;
			break;
		case GOT_CRC1:
			parseStat = PARSE_STATUS.IDDLE;
			this.crc2 = (char) (data & 0x0FF);
			crc = CheckSum.crc_init();
			crc = CheckSum.crc_accumulate((byte)(lenght>>8), crc);
			crc = CheckSum.crc_accumulate((byte)(lenght), crc);
			for(byte b : payload)
				crc = CheckSum.crc_accumulate(b, crc);
			if(crc1 == (crc&0x00FF) && crc2 == ((crc>>8)&0x00FF)){
				lastPayload = new byte[payload.length];
				for(int i =0; i<payload.length; i++)
					lastPayload[i] = payload[i];
				return true;
			}
			System.out.println("Bad CRC: "+lenght);
			for(byte b : payload)
				System.out.print(Integer.toHexString(b & 0x0FF) + ", ");
			System.out.println("\n\tWas:      " + Integer.toHexString(((crc2 & 0x0FF)<<8) | (crc1 & 0x0FF)));
			System.out.println("\tSould be: " + Integer.toHexString(crc));
			break;
		}
		return false;
	}
	
	public byte[] getPayload(){
		return lastPayload;
	}
	
	public static byte[] pack(byte[] payload){
		byte[] msg = new byte[MIN_MSG_LEN+payload.length];
		msg[0] = HEADER1;
		msg[1] = HEADER2;
		int len = payload.length;
		msg[2] = (byte) (len>>8);
		msg[3] = (byte) (len);
		char crc = CheckSum.crc_init();
		crc = CheckSum.crc_accumulate((byte)(len>>8), crc);
		crc = CheckSum.crc_accumulate((byte)len, crc);
		for(int i = 0; i<len; i++){
			crc = CheckSum.crc_accumulate(payload[i], crc);
			msg[i+4] = payload[i];
		}
		msg[msg.length-2] = (byte) (crc & 0x00FF);
		msg[msg.length-1] = (byte) ((crc>>8) & 0x00FF);
		return msg;
	}
}
