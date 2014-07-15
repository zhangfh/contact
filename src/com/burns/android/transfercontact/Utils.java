package com.burns.android.transfercontact;

public class Utils {
	
	public static byte[] copyOfRange(byte[] original, int from, int to) {   
	       int newLength = to - from;   
	       if (newLength < 0)   
	           throw new IllegalArgumentException(from + " > " + to);   
	       byte[] copy = new byte[newLength];   
	       System.arraycopy(original, from, copy, 0,   
	                        Math.min(original.length - from, newLength));   
	       return copy;   
	   } 
	public static StringBuilder ConvertByteToString(byte[] buffer, int bytes)
	{
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<bytes; i++)
		{
			sb.append(String.format("%02X ", buffer[i]));
  	
		}
		return sb;
	}
}
