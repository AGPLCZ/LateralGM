/*
 * Copyright (C) 2007, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import static org.lateralgm.main.Util.deRef;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.util.PropertyMap;

public class GmStreamEncoder extends StreamEncoder
	{
	protected int originalPos = -1;
	protected OutputStream originalStream;
	private int[] table = null;

	public GmStreamEncoder(OutputStream o)
		{
		super(o);
		}

	public GmStreamEncoder(File f) throws FileNotFoundException
		{
		super(f);
		}

	public GmStreamEncoder(String filePath) throws FileNotFoundException
		{
		super(filePath);
		}

	public void write(byte b[]) throws IOException
		{
		write(b,0,b.length);
		}

	public void write(byte b[], int off, int len) throws IOException
		{
		if (table != null)
			{
			for (int i = 0; i < len; i++)
				{
				int t = b[off + i] & 0xFF;
				int x = table[t + pos + i] & 0xFF;
				b[off + i] = (byte) x;
				}
			}
		super.write(b,off,len);
		}

	public void write(int b) throws IOException
		{
		if (table != null) b = table[b + pos] & 0xFF;
		super.write(b);
		}

	public void writeStr(String str) throws IOException
		{
		write4(str.length());
		write(str.getBytes("ascii")); //$NON-NLS-1$
		}

	public void writeStr1(String str) throws IOException
		{
		write(Math.min(str.length(),255));
		write(str.getBytes("ascii"),0,Math.min(str.length(),255)); //$NON-NLS-1$
		}

	public void writeBool(boolean val) throws IOException
		{
		write4(val ? 1 : 0);
		}

	public <P extends Enum<P>>void write4(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			write4((Integer) map.get(key));
		}

	public <P extends Enum<P>>void writeStr(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			writeStr((String) map.get(key));
		}

	public <P extends Enum<P>>void writeBool(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			writeBool((Boolean) map.get(key));
		}

	public <P extends Enum<P>>void writeD(PropertyMap<P> map, P...keys) throws IOException
		{
		for (P key : keys)
			writeD((Double) map.get(key));
		}

	public <R extends Resource<R,?>>void writeId(ResourceReference<R> id) throws IOException
		{
		writeId(id,-1);
		}

	public <R extends Resource<R,?>>void writeId(ResourceReference<R> id, int noneval)
			throws IOException
		{
		if (deRef(id) != null)
			write4(id.get().getId());
		else
			write4(noneval);
		}

	public void compress(byte[] data) throws IOException
		{
		Deflater compresser = new Deflater();
		compresser.setInput(data);
		compresser.finish();
		byte[] buffer = new byte[100];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!compresser.finished())
			{
			int len = compresser.deflate(buffer);
			baos.write(buffer,0,len);
			}
		write4(baos.size());
		write(baos.toByteArray());
		}

	public void beginDeflate()
		{
		originalStream = out;
		out = new GmStreamEncoder(new ByteArrayOutputStream());
		originalPos = pos;
		pos = 0;
		}

	/**
	 * Safely finishes this stream if it's a deflater, otherwise this call does nothing.
	 * This places the file reader after the end of the compressed data in the underlying stream.
	 */
	public void endDeflate() throws IOException
		{
		if (originalStream != null)
			{
			out.flush();
			GmStreamEncoder gse = (GmStreamEncoder) out;
			ByteArrayOutputStream baos = (ByteArrayOutputStream) gse.out;
			pos = originalPos;
			originalPos = -1;
			out = originalStream;
			originalStream = null;
			compress(baos.toByteArray());
			}
		}

	public void writeZlibImage(BufferedImage image) throws IOException
		{
		//Drop any alpha channel and convert to 3-byte to ensure that the image is bmp-compatible
		ColorConvertOp conv = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB),null);
		final BufferedImage dest = new BufferedImage(image.getWidth(),image.getHeight(),
				BufferedImage.TYPE_3BYTE_BGR);
		conv.filter(image,dest);

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ImageIO.write(dest,"bmp",data); //$NON-NLS-1$
		compress(data.toByteArray());
		}

	public void writeBGRAImage(BufferedImage image) throws IOException
		{
		int width = image.getWidth();
		int height = image.getHeight();

		//Because apparently there's no pretty way of fetching the
		//pixels of a BufferedImage in the desired format (BGRA)...
		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(image,0,0,width,height,pixels,0,width);
		try
			{
			pg.grabPixels();

			//ARGB => BGRA
			for (int p = 0; p < pixels.length; p++)
				{
				write(pixels[p] & 0xFF);
				write(pixels[p] >>> 8 & 0xFF);
				write(pixels[p] >>> 16 & 0xFF);
				write(pixels[p] >>> 24);
				}
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}

		/*int w = image.getWidth();
		int h = image.getHeight();
		WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,w,h,w * 4,4,
				new int[] { 2,1,0,3 },null); //2103 = RGBA ordering
		BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
		raster.setRect(dst.getRaster());

		byte[] buf = ((DataBufferByte) raster.getDataBuffer()).getData();
		out.write(buf);*/
		}

	/**
	 * GM7 Notice: since the first useful byte after the seed isn't encrypted,
	 * you may wish to delay setting the seed until that byte is retrieved,
	 * as implementing such functionality into these lower-level routines would add overhead
	 */
	public void setSeed(int s)
		{
		if (s >= 0)
			table = makeEncodeTable(s);
		else
			table = null;
		}

	protected static int[] makeEncodeTable(int seed)
		{
		int[] table = new int[256];
		int a = 6 + (seed % 250);
		int b = seed / 250;
		for (int i = 0; i < 256; i++)
			table[i] = i;
		for (int i = 1; i < 10001; i++)
			{
			int j = 1 + ((i * a + b) % 254);
			int t = table[j];
			table[j] = table[j + 1];
			table[j + 1] = t;
			}
		return table;
		}
	}
