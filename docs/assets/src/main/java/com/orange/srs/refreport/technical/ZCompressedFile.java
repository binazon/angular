package com.orange.srs.refreport.technical;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ejb.Stateless;

import com.googlecode.compress_j2me.lzc.LZCStream;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ZCompressedFile implements CompressedFile {

	@Override
	public boolean uncompress(String source, String destination, SOAContext soaContext) throws IOException {
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(source));
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(destination));

		LZCStream.uncompress(input, output);

		input.close();
		output.close();

		return true;
	}
}
