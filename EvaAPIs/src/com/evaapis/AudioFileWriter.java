/*     */ package com.evaapis;
/*     */ 
/*     */ import java.io.DataOutput;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public abstract class AudioFileWriter
/*     */ {
/*     */   public abstract void close()
/*     */     throws IOException;
/*     */ 
/*     */   public abstract void open(File paramFile)
/*     */     throws IOException;
/*     */ 
/*     */   public abstract void open(String paramString)
/*     */     throws IOException;
/*     */ 
/*     */   public abstract void writeHeader(String paramString)
/*     */     throws IOException;
/*     */ 
/*     */   public abstract void writePacket(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
/*     */     throws IOException;
/*     */ 
/*     */   public static int writeOggPageHeader(byte[] buf, int offset, int headerType, long granulepos, int streamSerialNumber, int pageCount, int packetCount, byte[] packetSizes)
/*     */   {
/* 112 */     writeString(buf, offset, "OggS");
/* 113 */     buf[(offset + 4)] = 0;
/* 114 */     buf[(offset + 5)] = (byte)headerType;
/* 115 */     writeLong(buf, offset + 6, granulepos);
/* 116 */     writeInt(buf, offset + 14, streamSerialNumber);
/* 117 */     writeInt(buf, offset + 18, pageCount);
/* 118 */     writeInt(buf, offset + 22, 0);
/* 119 */     buf[(offset + 26)] = (byte)packetCount;
/* 120 */     System.arraycopy(packetSizes, 0, buf, offset + 27, packetCount);
/*     */ 
/* 122 */     return (packetCount + 27);
/*     */   }
/*     */ 
/*     */   public static byte[] buildOggPageHeader(int headerType, long granulepos, int streamSerialNumber, int pageCount, int packetCount, byte[] packetSizes)
/*     */   {
/* 140 */     byte[] data = new byte[packetCount + 27];
/* 141 */     writeOggPageHeader(data, 0, headerType, granulepos, streamSerialNumber, pageCount, packetCount, packetSizes);
/*     */ 
/* 143 */     return data;
/*     */   }
/*     */ 
/*     */   public static int writeSpeexHeader(byte[] buf, int offset, int sampleRate, int mode, int channels, boolean vbr, int nframes)
/*     */   {
/* 161 */     writeString(buf, offset, "Speex   ");
/* 162 */     writeString(buf, offset + 8, "speex-1.0");
/* 163 */     System.arraycopy(new byte[11], 0, buf, offset + 17, 11);
/* 164 */     writeInt(buf, offset + 28, 1);
/* 165 */     writeInt(buf, offset + 32, 80);
/* 166 */     writeInt(buf, offset + 36, sampleRate);
/* 167 */     writeInt(buf, offset + 40, mode);
/* 168 */     writeInt(buf, offset + 44, 4);
/* 169 */     writeInt(buf, offset + 48, channels);
/* 170 */     writeInt(buf, offset + 52, 27*1024); // Iftah: was  -1,  changed to "27kbps" );
/* 171 */     writeInt(buf, offset + 56, 160 << mode);
/* 172 */     writeInt(buf, offset + 60, (vbr) ? 1 : 0);
/* 173 */     writeInt(buf, offset + 64, nframes);
/* 174 */     writeInt(buf, offset + 68, 0);
/* 175 */     writeInt(buf, offset + 72, 0);
/* 176 */     writeInt(buf, offset + 76, 0);
/* 177 */     return 80;
/*     */   }
/*     */ 
/*     */   public static byte[] buildSpeexHeader(int sampleRate, int mode, int channels, boolean vbr, int nframes)
/*     */   {
/* 192 */     byte[] data = new byte[80];
/* 193 */     writeSpeexHeader(data, 0, sampleRate, mode, channels, vbr, nframes);
/* 194 */     return data;
/*     */   }
/*     */ 
/*     */   public static int writeSpeexComment(byte[] buf, int offset, String comment)
/*     */   {
/* 206 */     int length = comment.length();
/* 207 */     writeInt(buf, offset, length);
/* 208 */     writeString(buf, offset + 4, comment);
/* 209 */     writeInt(buf, offset + length + 4, 0);
/* 210 */     return (length + 8);
/*     */   }
/*     */ 
/*     */   public static byte[] buildSpeexComment(String comment)
/*     */   {
/* 220 */     byte[] data = new byte[comment.length() + 8];
/* 221 */     writeSpeexComment(data, 0, comment);
/* 222 */     return data;
/*     */   }
/*     */ 
/*     */   public static void writeShort(DataOutput out, short v)
/*     */     throws IOException
/*     */   {
/* 234 */     out.writeByte(0xFF & v);
/* 235 */     out.writeByte(0xFF & v >>> 8);
/*     */   }
/*     */ 
/*     */   public static void writeInt(DataOutput out, int v)
/*     */     throws IOException
/*     */   {
/* 247 */     out.writeByte(0xFF & v);
/* 248 */     out.writeByte(0xFF & v >>> 8);
/* 249 */     out.writeByte(0xFF & v >>> 16);
/* 250 */     out.writeByte(0xFF & v >>> 24);
/*     */   }
/*     */ 
/*     */   public static void writeShort(OutputStream os, short v)
/*     */     throws IOException
/*     */   {
/* 262 */     os.write(0xFF & v);
/* 263 */     os.write(0xFF & v >>> 8);
/*     */   }
/*     */ 
/*     */   public static void writeInt(OutputStream os, int v)
/*     */     throws IOException
/*     */   {
/* 275 */     os.write(0xFF & v);
/* 276 */     os.write(0xFF & v >>> 8);
/* 277 */     os.write(0xFF & v >>> 16);
/* 278 */     os.write(0xFF & v >>> 24);
/*     */   }
/*     */ 
/*     */   public static void writeLong(OutputStream os, long v)
/*     */     throws IOException
/*     */   {
/* 290 */     os.write((int)(0xFF & v));
/* 291 */     os.write((int)(0xFF & v >>> 8));
/* 292 */     os.write((int)(0xFF & v >>> 16));
/* 293 */     os.write((int)(0xFF & v >>> 24));
/* 294 */     os.write((int)(0xFF & v >>> 32));
/* 295 */     os.write((int)(0xFF & v >>> 40));
/* 296 */     os.write((int)(0xFF & v >>> 48));
/* 297 */     os.write((int)(0xFF & v >>> 56));
/*     */   }
/*     */ 
/*     */   public static void writeShort(byte[] data, int offset, int v)
/*     */   {
/* 308 */     data[offset] = (byte)(0xFF & v);
/* 309 */     data[(offset + 1)] = (byte)(0xFF & v >>> 8);
/*     */   }
/*     */ 
/*     */   public static void writeInt(byte[] data, int offset, int v)
/*     */   {
/* 320 */     data[offset] = (byte)(0xFF & v);
/* 321 */     data[(offset + 1)] = (byte)(0xFF & v >>> 8);
/* 322 */     data[(offset + 2)] = (byte)(0xFF & v >>> 16);
/* 323 */     data[(offset + 3)] = (byte)(0xFF & v >>> 24);
/*     */   }
/*     */ 
/*     */   public static void writeLong(byte[] data, int offset, long v)
/*     */   {
/* 334 */     data[offset] = (byte)(int)(0xFF & v);
/* 335 */     data[(offset + 1)] = (byte)(int)(0xFF & v >>> 8);
/* 336 */     data[(offset + 2)] = (byte)(int)(0xFF & v >>> 16);
/* 337 */     data[(offset + 3)] = (byte)(int)(0xFF & v >>> 24);
/* 338 */     data[(offset + 4)] = (byte)(int)(0xFF & v >>> 32);
/* 339 */     data[(offset + 5)] = (byte)(int)(0xFF & v >>> 40);
/* 340 */     data[(offset + 6)] = (byte)(int)(0xFF & v >>> 48);
/* 341 */     data[(offset + 7)] = (byte)(int)(0xFF & v >>> 56);
/*     */   }
/*     */ 
/*     */   public static void writeString(byte[] data, int offset, String v)
/*     */   {
/* 352 */     byte[] str = v.getBytes();
/* 353 */     System.arraycopy(str, 0, data, offset, str.length);
/*     */   }
/*     */ }

/* Location:           /Users/iftah/Work/eva/android/EvaAPIs/libs/jspeex.jar
 * Qualified Name:     org.xiph.speex.AudioFileWriter
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */