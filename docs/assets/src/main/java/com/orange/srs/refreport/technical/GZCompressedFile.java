package com.orange.srs.refreport.technical;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.ejb.Stateless;

import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class GZCompressedFile implements CompressedFile {

	@Override
	public boolean uncompress(String source, String destination, SOAContext soaContext) throws IOException {

		BufferedInputStream instream = new BufferedInputStream(new FileInputStream(source));
		GZIPInputStream ginstream = new GZIPInputStream(instream);
		BufferedOutputStream outstream = new BufferedOutputStream(new FileOutputStream(destination));

		byte[] buf = new byte[32768];
		int len;
		while ((len = ginstream.read(buf)) > 0) {
			outstream.write(buf, 0, len);
		}

		instream.close();
		outstream.close();
		ginstream.close();

		return false;
	}
}
